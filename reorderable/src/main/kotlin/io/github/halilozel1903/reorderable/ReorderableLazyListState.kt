package io.github.halilozel1903.reorderable

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope

/** Reordering state of a `LazyColumn` or `LazyRow`, created with [rememberReorderableLazyListState]. */
@Stable
public class ReorderableLazyListState internal constructor(
    /** The list's scroll state. */
    public val lazyListState: LazyListState,
    scope: CoroutineScope,
    environment: ReorderEnvironment,
) : ReorderableLazyState(LazyListAdapter(lazyListState), scope, environment)

/**
 * Creates a [ReorderableLazyListState] for a `LazyColumn` or `LazyRow` that uses [lazyListState].
 *
 * ```
 * val listState = rememberLazyListState()
 * val reorderState = rememberReorderableLazyListState(listState) { from, to -> items.move(from, to) }
 * LazyColumn(state = listState) {
 *     items(items, key = { it.id }) { item ->
 *         ReorderableItem(reorderState, key = item.id) { isDragging ->
 *             Row { Text(item.title); Icon(DragIndicator, "Reorder", Modifier.draggableHandle()) }
 *         }
 *     }
 * }
 * ```
 *
 * @param lazyListState the state passed to the list.
 * @param isLocked items (by lazy layout index) that cannot move and cannot be passed, such as a pinned item.
 *   Items without a `ReorderableItem` (headers) and disabled ones are locked too.
 * @param autoScrollThreshold the list scrolls when the dragged item is this close to its start or end.
 * @param autoScrollMaxSpeed the scroll speed per second at the very edge; it eases in from the threshold.
 * @param hapticFeedbackEnabled vibrate when an item is picked up and every time it moves.
 * @param onDragStarted called with the item's index when a drag or keyboard reorder mode starts.
 * @param onDragStopped called with the start and end index when the item is dropped, a good moment to save.
 * @param onMove called every time the item passes another item: move your data from [from] to [to] (lazy
 *   layout indices: subtract leading items such as headers). `MutableList.move(from, to)` from
 *   `reorderable-core` does it.
 */
@Composable
public fun rememberReorderableLazyListState(
    lazyListState: LazyListState,
    isLocked: (index: Int) -> Boolean = { false },
    autoScrollThreshold: Dp = ReorderableDefaults.AutoScrollThreshold,
    autoScrollMaxSpeed: Dp = ReorderableDefaults.AutoScrollMaxSpeed,
    hapticFeedbackEnabled: Boolean = true,
    onDragStarted: (index: Int) -> Unit = {},
    onDragStopped: (from: Int, to: Int) -> Unit = { _, _ -> },
    onMove: (from: Int, to: Int) -> Unit,
): ReorderableLazyListState {
    val scope = rememberCoroutineScope()
    val environment = rememberReorderEnvironment(
        onMove = onMove,
        isLocked = isLocked,
        onDragStarted = onDragStarted,
        onDragStopped = onDragStopped,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        autoScrollThreshold = autoScrollThreshold,
        autoScrollMaxSpeed = autoScrollMaxSpeed,
    )
    return remember(lazyListState, scope, environment) {
        ReorderableLazyListState(lazyListState, scope, environment)
    }
}
