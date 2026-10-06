package io.github.halilozel1903.reorderable

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.halilozel1903.reorderable.core.ReorderActionTarget
import io.github.halilozel1903.reorderable.core.ReorderLabels

/** Scope of a [ReorderableItem]'s content: turns any composable into the item's drag handle. */
@Stable
public interface ReorderableItemScope {

    /** `true` while this item is in keyboard reorder mode (picked up with Enter or the D-pad center). */
    public val isKeyboardReordering: Boolean

    /**
     * Makes this element a drag handle: dragging it moves the item right away. Use it on a handle icon.
     *
     * @param enabled `false` turns the handle off.
     * @param interactionSource observes the handle's focus, to show a focus indication.
     * @param keyboardReorder the handle is focusable and Enter, Space or the D-pad center picks the item up for
     *   keyboard reordering (arrow keys move it, Enter drops it, Escape puts it back).
     */
    public fun Modifier.draggableHandle(
        enabled: Boolean = true,
        interactionSource: MutableInteractionSource? = null,
        keyboardReorder: Boolean = true,
    ): Modifier

    /**
     * Makes this element a drag handle that needs a long press first, so a quick swipe still scrolls the list.
     * Use it on the whole item. With [keyboardReorder], holding Enter or the D-pad center picks the item up while
     * a short press is left to the element (a click, for example).
     */
    public fun Modifier.longPressDraggableHandle(
        enabled: Boolean = true,
        interactionSource: MutableInteractionSource? = null,
        keyboardReorder: Boolean = true,
    ): Modifier
}

/**
 * An item of a `LazyColumn` or `LazyRow` that can be reordered. Call it as the root of each item, with the same
 * [key] as the list's `key`:
 *
 * ```
 * items(tracks, key = { it.id }) { track ->
 *     ReorderableItem(reorderState, key = track.id) { isDragging ->
 *         TrackRow(track, handle = Modifier.draggableHandle())
 *     }
 * }
 * ```
 *
 * The dragged item is lifted ([liftedElevation], [liftedScale]) and drawn above the others, which animate out of
 * its way. Accessibility services get "Move up", "Move down", "Move to top" and "Move to bottom" actions (or
 * left, right, start and end in a `LazyRow`), labelled by [labels].
 *
 * @param enabled `false` keeps the item in place: it cannot be dragged and works like a locked item.
 * @param shape the shape of the lifted item's shadow.
 * @param animateItemModifier placement animation for the items that make room; `Modifier` turns it off.
 * @param content the item, with `isDragging` while it is dragged or in keyboard reorder mode.
 */
@Composable
public fun LazyItemScope.ReorderableItem(
    state: ReorderableLazyListState,
    key: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    liftedElevation: Dp = ReorderableDefaults.LiftedElevation,
    liftedScale: Float = ReorderableDefaults.LiftedScale,
    shape: Shape = RectangleShape,
    labels: ReorderLabels = LocalReorderLabels.current,
    animateItemModifier: Modifier = Modifier.animateItem(),
    content: @Composable ReorderableItemScope.(isDragging: Boolean) -> Unit,
) {
    ReorderableItemLayout(
        state = state,
        key = key,
        modifier = modifier,
        enabled = enabled,
        liftedElevation = liftedElevation,
        liftedScale = liftedScale,
        shape = shape,
        labels = labels,
        animateItemModifier = animateItemModifier,
        content = content,
    )
}

/**
 * An item of a `LazyVerticalGrid` that can be reordered; see the `LazyItemScope` overload. Accessibility
 * services get "Move up", "Move down", "Move left", "Move right", "Move to top" and "Move to bottom".
 */
@Composable
public fun LazyGridItemScope.ReorderableItem(
    state: ReorderableLazyGridState,
    key: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    liftedElevation: Dp = ReorderableDefaults.LiftedElevation,
    liftedScale: Float = ReorderableDefaults.LiftedScale,
    shape: Shape = RectangleShape,
    labels: ReorderLabels = LocalReorderLabels.current,
    animateItemModifier: Modifier = Modifier.animateItem(),
    content: @Composable ReorderableItemScope.(isDragging: Boolean) -> Unit,
) {
    ReorderableItemLayout(
        state = state,
        key = key,
        modifier = modifier,
        enabled = enabled,
        liftedElevation = liftedElevation,
        liftedScale = liftedScale,
        shape = shape,
        labels = labels,
        animateItemModifier = animateItemModifier,
        content = content,
    )
}

@Composable
private fun ReorderableItemLayout(
    state: ReorderableLazyState,
    key: Any,
    modifier: Modifier,
    enabled: Boolean,
    liftedElevation: Dp,
    liftedScale: Float,
    shape: Shape,
    labels: ReorderLabels,
    animateItemModifier: Modifier,
    content: @Composable ReorderableItemScope.(isDragging: Boolean) -> Unit,
) {
    val isPointerDragging = state.draggingKey == key
    val isSettling = state.settlingKey == key
    val isKeyboardReordering = state.keyboardReorderKey == key
    val isDragging = isPointerDragging || isKeyboardReordering
    val followsPointer = isPointerDragging || isSettling

    DisposableEffect(state, key, enabled) {
        state.register(key, enabled)
        onDispose { state.unregister(key) }
    }

    val elevation by animateDpAsState(if (isDragging) liftedElevation else 0.dp, label = "ReorderableItemElevation")
    val scale by animateFloatAsState(if (isDragging) liftedScale else 1f, label = "ReorderableItemScale")
    val actionTargets by remember(state, key) { derivedStateOf { state.availableActions(key) } }
    val customActions = if (enabled) accessibilityActions(state, key, actionTargets, labels) else emptyList()
    val focusMemory = remember { FocusMemory() }

    Box(
        modifier = modifier
            .then(if (followsPointer || isKeyboardReordering) Modifier.zIndex(1f) else Modifier)
            // The dragged item follows the pointer itself; a placement animation would pull it back to its slot.
            .then(if (followsPointer) Modifier else animateItemModifier)
            .graphicsLayer {
                val translation = state.translationOf(key)
                translationX = translation.x
                translationY = translation.y
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = elevation, shape = shape, clip = false)
            .onFocusChanged { focusState ->
                if (focusMemory.hadFocus && !focusState.hasFocus) state.onItemFocusLost(key)
                focusMemory.hadFocus = focusState.hasFocus
            }
            .onPreviewKeyEvent { event -> enabled && state.onItemKeyEvent(key, event) }
            .semantics {
                if (customActions.isNotEmpty()) this.customActions = customActions
                if (isDragging) stateDescription = labels.reordering
            },
    ) {
        val scope = remember(state, key, enabled) { ReorderableItemScopeImpl(state, key, enabled) }
        scope.content(isDragging)
    }
}

private class FocusMemory {
    var hadFocus: Boolean = false
}

private fun accessibilityActions(
    state: ReorderableLazyState,
    key: Any,
    targets: List<ReorderActionTarget>,
    labels: ReorderLabels,
): List<CustomAccessibilityAction> = targets.map { target ->
    CustomAccessibilityAction(labels.labelFor(target.action)) { state.move(key, target.action) }
}

private class ReorderableItemScopeImpl(
    private val state: ReorderableLazyState,
    private val key: Any,
    private val itemEnabled: Boolean,
) : ReorderableItemScope {

    override val isKeyboardReordering: Boolean
        get() = state.keyboardReorderKey == key

    override fun Modifier.draggableHandle(
        enabled: Boolean,
        interactionSource: MutableInteractionSource?,
        keyboardReorder: Boolean,
    ): Modifier {
        if (!enabled || !itemEnabled) return this
        return this
            .then(keyboardModifier(keyboardReorder, interactionSource, longPress = false))
            .pointerInput(state, key) {
                var active = false
                detectDragGestures(
                    onDragStart = { active = state.startDrag(key) },
                    onDragEnd = {
                        if (active) state.endDrag()
                        active = false
                    },
                    onDragCancel = {
                        if (active) state.endDrag()
                        active = false
                    },
                    onDrag = { change, dragAmount ->
                        if (active) {
                            change.consume()
                            state.dragBy(dragAmount)
                        }
                    },
                )
            }
    }

    override fun Modifier.longPressDraggableHandle(
        enabled: Boolean,
        interactionSource: MutableInteractionSource?,
        keyboardReorder: Boolean,
    ): Modifier {
        if (!enabled || !itemEnabled) return this
        return this
            .then(keyboardModifier(keyboardReorder, interactionSource, longPress = true))
            .pointerInput(state, key) {
                var active = false
                detectDragGesturesAfterLongPress(
                    onDragStart = { active = state.startDrag(key) },
                    onDragEnd = {
                        if (active) state.endDrag()
                        active = false
                    },
                    onDragCancel = {
                        if (active) state.endDrag()
                        active = false
                    },
                    onDrag = { change, dragAmount ->
                        if (active) {
                            change.consume()
                            state.dragBy(dragAmount)
                        }
                    },
                )
            }
    }

    private fun keyboardModifier(
        keyboardReorder: Boolean,
        interactionSource: MutableInteractionSource?,
        longPress: Boolean,
    ): Modifier {
        if (!keyboardReorder) return Modifier
        return Modifier
            .onKeyEvent { event -> state.onHandleKeyEvent(key, event, longPress) }
            .focusable(interactionSource = interactionSource)
    }
}
