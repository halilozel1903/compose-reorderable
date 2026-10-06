package io.github.halilozel1903.reorderable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.geometry.Offset

/**
 * A visible item in the lazy layout's own coordinates (logical: before mirroring for right-to-left layouts and
 * `reverseLayout`). [lane] is the column of a vertical grid or the row of a horizontal grid, 0 for lists.
 */
internal class LazyItem(
    val index: Int,
    val key: Any,
    val offset: Offset,
    val width: Float,
    val height: Float,
    val lane: Int,
)

/** What the reordering logic needs from a `LazyListState` or a `LazyGridState`. */
internal interface LazyLayoutAdapter {
    val scrollableState: ScrollableState
    val isGrid: Boolean
    val isVertical: Boolean
    val reverseLayout: Boolean
    val viewportStart: Int
    val viewportEnd: Int
    val totalItemsCount: Int
    val firstVisibleItemIndex: Int
    val firstVisibleItemScrollOffset: Int

    fun visibleItems(): List<LazyItem>

    /** Keeps [index] at [scrollOffset] on the next layout instead of following the first visible item's key. */
    fun requestScrollToItem(index: Int, scrollOffset: Int)

    suspend fun scrollToItem(index: Int)
}

@OptIn(ExperimentalFoundationApi::class)
internal class LazyListAdapter(private val state: LazyListState) : LazyLayoutAdapter {
    override val scrollableState: ScrollableState get() = state
    override val isGrid: Boolean get() = false
    override val isVertical: Boolean get() = state.layoutInfo.orientation == Orientation.Vertical
    override val reverseLayout: Boolean get() = state.layoutInfo.reverseLayout
    override val viewportStart: Int get() = state.layoutInfo.viewportStartOffset
    override val viewportEnd: Int get() = state.layoutInfo.viewportEndOffset
    override val totalItemsCount: Int get() = state.layoutInfo.totalItemsCount
    override val firstVisibleItemIndex: Int get() = state.firstVisibleItemIndex
    override val firstVisibleItemScrollOffset: Int get() = state.firstVisibleItemScrollOffset

    override fun visibleItems(): List<LazyItem> {
        val info = state.layoutInfo
        val vertical = info.orientation == Orientation.Vertical
        val crossSize = (if (vertical) info.viewportSize.width else info.viewportSize.height).toFloat()
        return info.visibleItemsInfo.map { item ->
            if (vertical) {
                LazyItem(item.index, item.key, Offset(0f, item.offset.toFloat()), crossSize, item.size.toFloat(), 0)
            } else {
                LazyItem(item.index, item.key, Offset(item.offset.toFloat(), 0f), item.size.toFloat(), crossSize, 0)
            }
        }
    }

    override fun requestScrollToItem(index: Int, scrollOffset: Int) {
        state.requestScrollToItem(index, scrollOffset)
    }

    override suspend fun scrollToItem(index: Int) {
        state.scrollToItem(index)
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal class LazyGridAdapter(private val state: LazyGridState) : LazyLayoutAdapter {
    override val scrollableState: ScrollableState get() = state
    override val isGrid: Boolean get() = true
    override val isVertical: Boolean get() = state.layoutInfo.orientation == Orientation.Vertical
    override val reverseLayout: Boolean get() = state.layoutInfo.reverseLayout
    override val viewportStart: Int get() = state.layoutInfo.viewportStartOffset
    override val viewportEnd: Int get() = state.layoutInfo.viewportEndOffset
    override val totalItemsCount: Int get() = state.layoutInfo.totalItemsCount
    override val firstVisibleItemIndex: Int get() = state.firstVisibleItemIndex
    override val firstVisibleItemScrollOffset: Int get() = state.firstVisibleItemScrollOffset

    override fun visibleItems(): List<LazyItem> {
        val info = state.layoutInfo
        val vertical = info.orientation == Orientation.Vertical
        return info.visibleItemsInfo.map { item ->
            LazyItem(
                index = item.index,
                key = item.key,
                offset = Offset(item.offset.x.toFloat(), item.offset.y.toFloat()),
                width = item.size.width.toFloat(),
                height = item.size.height.toFloat(),
                lane = if (vertical) item.column else item.row,
            )
        }
    }

    override fun requestScrollToItem(index: Int, scrollOffset: Int) {
        state.requestScrollToItem(index, scrollOffset)
    }

    override suspend fun scrollToItem(index: Int) {
        state.scrollToItem(index)
    }
}
