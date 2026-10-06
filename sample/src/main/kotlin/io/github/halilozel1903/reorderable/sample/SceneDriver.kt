package io.github.halilozel1903.reorderable.sample

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import io.github.halilozel1903.reorderable.ReorderableLazyState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/*
 * Screenshot helpers. A drag cannot be performed reliably on a CI emulator, so the scenes call the same state
 * functions the drag handles call (startDrag, dragBy, startKeyboardReorder, move) and then simply stop: the item
 * stays lifted with its gap, exactly as in the middle of a real drag.
 */

suspend fun awaitVisibleItem(state: LazyListState, key: Any): LazyListItemInfo =
    snapshotFlow { state.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } }.filterNotNull().first()

suspend fun awaitVisibleItem(state: LazyGridState, key: Any): LazyGridItemInfo =
    snapshotFlow { state.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } }.filterNotNull().first()

/** Lets the first layout and enter animations finish. */
suspend fun settle() {
    delay(400)
    withFrameNanos { }
}

/** Lifts the item with [key] and drags it by [total] pixels over a few frames, the way a finger would. */
suspend fun ReorderableLazyState.dragInSteps(key: Any, total: Offset, steps: Int = 12) {
    settle()
    if (!startDrag(key)) return
    repeat(steps) {
        withFrameNanos { }
        dragBy(total / steps.toFloat())
    }
    // A move is laid out in the next frame; nudge once more so the final position is evaluated.
    repeat(3) { withFrameNanos { } }
    dragBy(Offset.Zero)
}
