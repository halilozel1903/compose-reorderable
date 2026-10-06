package io.github.halilozel1903.reorderable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import io.github.halilozel1903.reorderable.core.AutoScroll
import io.github.halilozel1903.reorderable.core.ItemRect
import io.github.halilozel1903.reorderable.core.ItemSpan
import io.github.halilozel1903.reorderable.core.ReorderAction
import io.github.halilozel1903.reorderable.core.ReorderActionTarget
import io.github.halilozel1903.reorderable.core.ReorderLayout
import io.github.halilozel1903.reorderable.core.ReorderNavigation
import io.github.halilozel1903.reorderable.core.ReorderTargets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Reordering state of a lazy list or grid, shared by [ReorderableLazyListState] and [ReorderableLazyGridState].
 *
 * An item is moved in three ways:
 * - **Dragging** with a handle ([ReorderableItemScope.draggableHandle] or
 *   [ReorderableItemScope.longPressDraggableHandle]). The item is lifted and follows the pointer, the other items
 *   make room, `onMove` is called every time it passes another item and the list scrolls near its edges.
 * - **Keyboard reorder mode** for TV remotes and hardware keyboards: Enter (or the D-pad center) on a handle picks
 *   the item up, the arrow keys move it and Enter drops it. Escape puts it back where it was. Ctrl + arrow keys
 *   move the focused item directly.
 * - **Accessibility actions** ("Move up", "Move to top"...) that TalkBack and Switch Access offer on each item.
 *
 * [startDrag], [dragBy], [endDrag], [startKeyboardReorder] and [move] drive the same logic from code, for tests,
 * demos and screenshots.
 */
@Stable
public abstract class ReorderableLazyState internal constructor(
    internal val adapter: LazyLayoutAdapter,
    private val scope: CoroutineScope,
    private val environment: ReorderEnvironment,
) {
    /** Key of the item being dragged with a pointer, or `null`. */
    public var draggingKey: Any? by mutableStateOf<Any?>(null)
        private set

    /** Key of the item in keyboard reorder mode, or `null`. */
    public var keyboardReorderKey: Any? by mutableStateOf<Any?>(null)
        private set

    /** Key of the item animating back into its slot after a drag, or `null`. */
    internal var settlingKey: Any? by mutableStateOf<Any?>(null)
        private set

    private var settleAnimatable: Animatable<Offset, AnimationVector2D>? by
        mutableStateOf<Animatable<Offset, AnimationVector2D>?>(null)

    /** How far the pointer has moved since the drag started, in logical coordinates. */
    private var dragDelta: Offset by mutableStateOf(Offset.Zero)

    /** The dragged item's slot when the drag started, in logical coordinates. */
    private var initialSlot: Offset = Offset.Zero
    private var draggingIndex: Int = -1
    private var dragStartIndex: Int = -1
    private var awaitingLayout: Boolean = false
    private var keyboardIndex: Int = -1
    private var keyboardStartIndex: Int = -1
    private val itemEnabled = HashMap<Any, Boolean>()
    private var autoScrollJob: Job? = null
    private var settleJob: Job? = null

    /** `true` while an item is dragged with a pointer. */
    public val isAnyItemDragging: Boolean get() = draggingKey != null

    /** `true` while an item is in keyboard reorder mode. */
    public val isKeyboardReordering: Boolean get() = keyboardReorderKey != null

    /** `true` when the item with [key] is dragged or in keyboard reorder mode. */
    public fun isDragging(key: Any): Boolean = draggingKey == key || keyboardReorderKey == key

    // region Pointer dragging

    /**
     * Lifts the visible item with [key] as if a drag started on its handle. Returns `false` when the item is not
     * visible, is locked or disabled, or another item is being dragged.
     */
    public fun startDrag(key: Any): Boolean {
        if (draggingKey != null) return false
        val items = adapter.visibleItems()
        val item = items.firstOrNull { it.key == key } ?: return false
        if (isLockedIndex(item.index, items)) return false
        if (keyboardReorderKey != null) stopKeyboardReorder()
        finishSettling()
        initialSlot = item.offset
        dragDelta = Offset.Zero
        draggingIndex = item.index
        dragStartIndex = item.index
        awaitingLayout = false
        draggingKey = key
        haptic(HapticFeedbackType.LongPress)
        environment.onDragStarted.value(item.index)
        startAutoScroll()
        return true
    }

    /**
     * Moves the dragged item by [delta] pixels on screen. Lists only use the part along their axis. Calls
     * `onMove` when the item passes other items.
     */
    public fun dragBy(delta: Offset) {
        if (draggingKey == null) return
        val logical = mirror(delta)
        dragDelta += when {
            adapter.isGrid -> logical
            adapter.isVertical -> Offset(0f, logical.y)
            else -> Offset(logical.x, 0f)
        }
        updateTarget()
    }

    /** Drops the dragged item: it animates into its slot and `onDragStopped` is called. */
    public fun endDrag() {
        val key = draggingKey ?: return
        val offset = translationOf(key)
        finishDrag(key, offset)
    }

    /** Puts the dragged item back where the drag started. */
    public fun cancelDrag() {
        val key = draggingKey ?: return
        if (draggingIndex != dragStartIndex) {
            moveItem(draggingIndex, dragStartIndex)
            draggingIndex = dragStartIndex
        }
        // The item's slot is back where the drag started, so the pointer's travel is the offset to animate away.
        finishDrag(key, mirror(dragDelta))
    }

    private fun finishDrag(key: Any, offset: Offset) {
        val from = dragStartIndex
        val to = draggingIndex
        autoScrollJob?.cancel()
        autoScrollJob = null
        draggingKey = null
        dragDelta = Offset.Zero
        settle(key, offset)
        environment.onDragStopped.value(from, to)
    }

    private fun updateTarget() {
        val key = draggingKey ?: return
        val items = adapter.visibleItems()
        val dragged = items.firstOrNull { it.key == key } ?: return
        if (dragged.index != draggingIndex) {
            // Wait until the layout shows the last move; adopt the index if the data changed under the drag.
            if (awaitingLayout) return
            draggingIndex = dragged.index
        }
        awaitingLayout = false
        val position = initialSlot + dragDelta
        val locked: (Int) -> Boolean = { isLockedIndex(it, items) }
        val target = when {
            adapter.isGrid -> ReorderTargets.gridTarget(
                draggedIndex = dragged.index,
                dragged = ItemRect(dragged.index, position.x, position.y, dragged.width, dragged.height),
                items = items.map { ItemRect(it.index, it.offset.x, it.offset.y, it.width, it.height) },
                isLocked = locked,
            )
            adapter.isVertical -> ReorderTargets.listTarget(
                draggedIndex = dragged.index,
                draggedStart = position.y,
                draggedSize = dragged.height,
                items = items.map { ItemSpan(it.index, it.offset.y, it.height) },
                isLocked = locked,
            )
            else -> ReorderTargets.listTarget(
                draggedIndex = dragged.index,
                draggedStart = position.x,
                draggedSize = dragged.width,
                items = items.map { ItemSpan(it.index, it.offset.x, it.width) },
                isLocked = locked,
            )
        }
        if (target != dragged.index) {
            moveItem(dragged.index, target)
            draggingIndex = target
            awaitingLayout = true
        }
    }

    // endregion

    // region Keyboard and accessibility

    /**
     * Puts the visible item with [key] in keyboard reorder mode: arrow keys and the D-pad move it until
     * [stopKeyboardReorder]. Returns `false` when the item is not visible, locked or disabled, or an item is
     * being dragged.
     */
    public fun startKeyboardReorder(key: Any): Boolean {
        if (draggingKey != null) return false
        if (keyboardReorderKey == key) return true
        val items = adapter.visibleItems()
        val item = items.firstOrNull { it.key == key } ?: return false
        if (isLockedIndex(item.index, items)) return false
        if (keyboardReorderKey != null) stopKeyboardReorder()
        keyboardIndex = item.index
        keyboardStartIndex = item.index
        keyboardReorderKey = key
        haptic(HapticFeedbackType.LongPress)
        environment.onDragStarted.value(item.index)
        return true
    }

    /** Leaves keyboard reorder mode. With [cancel] the item goes back to where it was picked up. */
    public fun stopKeyboardReorder(cancel: Boolean = false) {
        if (keyboardReorderKey == null) return
        if (cancel && keyboardIndex != keyboardStartIndex) {
            moveItem(keyboardIndex, keyboardStartIndex)
            keyboardIndex = keyboardStartIndex
            bringIntoView(keyboardStartIndex)
        }
        keyboardReorderKey = null
        environment.onDragStopped.value(keyboardStartIndex, keyboardIndex)
    }

    /**
     * Moves the item with [key] by [action] ("Move up", "Move to top"...), as the keyboard and accessibility
     * services do. Up, down, left and right are directions on screen. Returns `false` when the move is not
     * possible.
     */
    public fun move(key: Any, action: ReorderAction): Boolean {
        if (draggingKey != null) return false
        val items = adapter.visibleItems()
        val index = if (key == keyboardReorderKey) {
            keyboardIndex
        } else {
            items.firstOrNull { it.key == key }?.index ?: return false
        }
        val target = targetFor(action, index, items) ?: return false
        moveItem(index, target)
        if (key == keyboardReorderKey) keyboardIndex = target
        bringIntoView(target)
        return true
    }

    /** The moves possible for the visible item with [key], in reading order. */
    public fun availableActions(key: Any): List<ReorderActionTarget> {
        val items = adapter.visibleItems()
        val item = items.firstOrNull { it.key == key } ?: return emptyList()
        val actions = when {
            adapter.isGrid && adapter.isVertical -> VerticalGridActions
            adapter.isGrid -> HorizontalGridActions
            adapter.isVertical -> ColumnActions
            else -> RowActions
        }
        val targets = actions.mapNotNull { action ->
            targetFor(action, item.index, items)?.let { ReorderActionTarget(action, it) }
        }
        val stepTargets = targets.filter { it.action.isStep }.map { it.targetIndex }.toSet()
        return targets.filter { it.action.isStep || it.targetIndex !in stepTargets }
    }

    private fun targetFor(action: ReorderAction, index: Int, items: List<LazyItem>): Int? {
        val logical = toLogicalAction(action)
        val locked: (Int) -> Boolean = { isLockedIndex(it, items) }
        val layout = when {
            adapter.isGrid -> ReorderLayout.grid((items.maxOfOrNull { it.lane } ?: 0) + 1)
            adapter.isVertical -> ReorderLayout.VerticalList
            else -> ReorderLayout.HorizontalList
        }
        // A horizontal grid is handled as a transposed vertical one (see toLogicalAction), never mirrored.
        val rtl = if (adapter.isGrid && !adapter.isVertical) false else mirrorX
        return ReorderNavigation.target(logical, index, adapter.totalItemsCount, layout, rtl, locked)
    }

    /** Turns a direction on screen into the move in index order. */
    private fun toLogicalAction(action: ReorderAction): ReorderAction {
        if (adapter.isGrid && !adapter.isVertical) {
            // Column by column: one step up or down stays in the column, left and right jump a whole column.
            return when (action) {
                ReorderAction.MoveUp -> ReorderAction.MoveLeft
                ReorderAction.MoveDown -> ReorderAction.MoveRight
                ReorderAction.MoveLeft -> if (mirrorX) ReorderAction.MoveDown else ReorderAction.MoveUp
                ReorderAction.MoveRight -> if (mirrorX) ReorderAction.MoveUp else ReorderAction.MoveDown
                ReorderAction.MoveToStart, ReorderAction.MoveToTop -> ReorderAction.MoveToTop
                ReorderAction.MoveToEnd, ReorderAction.MoveToBottom -> ReorderAction.MoveToBottom
            }
        }
        if (mirrorY) {
            return when (action) {
                ReorderAction.MoveUp -> ReorderAction.MoveDown
                ReorderAction.MoveDown -> ReorderAction.MoveUp
                ReorderAction.MoveToTop -> ReorderAction.MoveToBottom
                ReorderAction.MoveToBottom -> ReorderAction.MoveToTop
                else -> action
            }
        }
        return action
    }

    internal fun toggleKeyboardReorder(key: Any): Boolean =
        if (keyboardReorderKey == key) {
            stopKeyboardReorder()
            true
        } else {
            startKeyboardReorder(key)
        }

    /** Key events that reach an item (preview, so they are seen before the focused child). */
    internal fun onItemKeyEvent(key: Any, event: KeyEvent): Boolean {
        val step = event.key.toStepAction()
        val down = event.type == KeyEventType.KeyDown
        if (keyboardReorderKey == key) {
            return when {
                step != null -> {
                    if (down) move(key, step)
                    true
                }
                event.key in ConfirmKeys -> {
                    // Ignore the repeats of the key that picked the item up (a long press on a TV remote).
                    if (down && event.repeatCount == 0) stopKeyboardReorder()
                    true
                }
                event.key == Key.Escape -> {
                    if (down) stopKeyboardReorder(cancel = true)
                    true
                }
                event.key == Key.Back -> {
                    if (down) stopKeyboardReorder()
                    true
                }
                else -> false
            }
        }
        if (step != null && down && event.isCtrlPressed && draggingKey == null) {
            return move(key, step)
        }
        return false
    }

    /** Key events on a focused handle: Enter, Space or the D-pad center picks the item up. */
    internal fun onHandleKeyEvent(key: Any, event: KeyEvent, longPress: Boolean): Boolean {
        if (event.type != KeyEventType.KeyDown || event.key !in ConfirmKeys) return false
        if (keyboardReorderKey == key) return false
        val expectedRepeat = if (longPress) 1 else 0
        if (event.repeatCount != expectedRepeat) return false
        return startKeyboardReorder(key)
    }

    internal fun onItemFocusLost(key: Any) {
        if (keyboardReorderKey == key) stopKeyboardReorder()
    }

    // endregion

    // region Items

    internal fun register(key: Any, enabled: Boolean) {
        itemEnabled[key] = enabled
    }

    internal fun unregister(key: Any) {
        itemEnabled.remove(key)
    }

    /**
     * Locked: by the `isLocked` callback, or a visible item that is disabled or not a `ReorderableItem` at all
     * (a header, for example).
     */
    private fun isLockedIndex(index: Int, items: List<LazyItem>): Boolean {
        if (environment.isLocked.value(index)) return true
        val item = items.firstOrNull { it.index == index } ?: return false
        return itemEnabled[item.key] != true
    }

    /** Where the item with [key] is drawn relative to its slot, in pixels on screen. */
    internal fun translationOf(key: Any): Offset {
        if (key == draggingKey) {
            val item = adapter.visibleItems().firstOrNull { it.key == key } ?: return Offset.Zero
            return mirror(initialSlot + dragDelta - item.offset)
        }
        if (key == settlingKey) return settleAnimatable?.value ?: Offset.Zero
        return Offset.Zero
    }

    private fun moveItem(from: Int, to: Int) {
        val first = adapter.firstVisibleItemIndex
        val firstOffset = adapter.firstVisibleItemScrollOffset
        environment.onMove.value(from, to)
        // A lazy layout keeps its first visible item in place by key; moving that item would scroll the list.
        if (from == first || to == first) adapter.requestScrollToItem(first, firstOffset)
        haptic(HapticFeedbackType.TextHandleMove)
    }

    private fun settle(key: Any, from: Offset) {
        settleJob?.cancel()
        val animatable = Animatable(from, Offset.VectorConverter)
        settleAnimatable = animatable
        settlingKey = key
        settleJob = scope.launch {
            try {
                animatable.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMediumLow))
            } finally {
                if (settleAnimatable === animatable) {
                    settlingKey = null
                    settleAnimatable = null
                }
            }
        }
    }

    private fun finishSettling() {
        settleJob?.cancel()
        settleJob = null
        settlingKey = null
        settleAnimatable = null
    }

    /** Scrolls so the item at [index] is fully visible once the last move is laid out. */
    private fun bringIntoView(index: Int) {
        scope.launch {
            // The move is applied in the next frame's layout.
            withFrameNanos { }
            withFrameNanos { }
            val item = adapter.visibleItems().firstOrNull { it.index == index }
            if (item == null) {
                adapter.scrollToItem(index)
                return@launch
            }
            val start = mainAxis(item.offset)
            val end = start + mainSize(item)
            val viewportStart = adapter.viewportStart.toFloat()
            val viewportEnd = adapter.viewportEnd.toFloat()
            val delta = when {
                start < viewportStart -> start - viewportStart
                end > viewportEnd -> min(end - viewportEnd, start - viewportStart)
                else -> 0f
            }
            if (delta != 0f) adapter.scrollableState.scrollBy(delta)
        }
    }

    // endregion

    // region Auto-scroll

    private fun startAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = scope.launch {
            while (isActive && draggingKey != null) {
                // Sleep without asking for frames until the item is dragged towards an edge.
                snapshotFlow { autoScrollVelocity() != 0f }.first { it }
                var previous = withFrameNanos { it }
                while (isActive && draggingKey != null) {
                    val velocity = autoScrollVelocity()
                    if (velocity == 0f) break
                    val now = withFrameNanos { it }
                    val delta = AutoScroll.frameDelta(velocity, now - previous)
                    previous = now
                    try {
                        adapter.scrollableState.scrollBy(delta)
                    } catch (e: CancellationException) {
                        // Another scroll (such as keeping the first item in place) took over; keep going.
                        ensureActive()
                    }
                    updateTarget()
                }
            }
        }
    }

    private fun autoScrollVelocity(): Float {
        val key = draggingKey ?: return 0f
        val item = adapter.visibleItems().firstOrNull { it.key == key } ?: return 0f
        val start = mainAxis(initialSlot + dragDelta)
        val velocity = AutoScroll.velocity(
            itemStart = start,
            itemEnd = start + mainSize(item),
            viewportStart = adapter.viewportStart.toFloat(),
            viewportEnd = adapter.viewportEnd.toFloat(),
            threshold = environment.autoScrollThreshold.value,
            maxSpeed = environment.autoScrollMaxSpeed.value,
            canScrollBackward = adapter.scrollableState.canScrollBackward,
            canScrollForward = adapter.scrollableState.canScrollForward,
        )
        // Only scroll towards the edge the item is being dragged to, not when a drag starts next to an edge.
        val travel = mainAxis(dragDelta)
        return when {
            velocity > 0f && travel > 0f -> velocity
            velocity < 0f && travel < 0f -> velocity
            else -> 0f
        }
    }

    // endregion

    // region Coordinates

    private val mirrorX: Boolean
        get() = if (adapter.isVertical) environment.isRtl.value else adapter.reverseLayout != environment.isRtl.value

    private val mirrorY: Boolean
        get() = adapter.isVertical && adapter.reverseLayout

    /** Screen pixels to the layout's logical coordinates and back (the conversion is its own inverse). */
    private fun mirror(offset: Offset): Offset =
        Offset(if (mirrorX) -offset.x else offset.x, if (mirrorY) -offset.y else offset.y)

    private fun mainAxis(offset: Offset): Float = if (adapter.isVertical) offset.y else offset.x

    private fun mainSize(item: LazyItem): Float = if (adapter.isVertical) item.height else item.width

    private fun haptic(type: HapticFeedbackType) {
        environment.haptic.value?.performHapticFeedback(type)
    }

    // endregion

    private companion object {
        val ColumnActions = listOf(
            ReorderAction.MoveToTop, ReorderAction.MoveUp, ReorderAction.MoveDown, ReorderAction.MoveToBottom,
        )
        val RowActions = listOf(
            ReorderAction.MoveToStart, ReorderAction.MoveLeft, ReorderAction.MoveRight, ReorderAction.MoveToEnd,
        )
        val VerticalGridActions = listOf(
            ReorderAction.MoveToTop, ReorderAction.MoveUp, ReorderAction.MoveLeft,
            ReorderAction.MoveRight, ReorderAction.MoveDown, ReorderAction.MoveToBottom,
        )
        val HorizontalGridActions = listOf(
            ReorderAction.MoveToStart, ReorderAction.MoveLeft, ReorderAction.MoveUp,
            ReorderAction.MoveDown, ReorderAction.MoveRight, ReorderAction.MoveToEnd,
        )
    }
}
