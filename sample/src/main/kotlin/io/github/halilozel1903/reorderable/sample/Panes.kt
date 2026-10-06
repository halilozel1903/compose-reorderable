package io.github.halilozel1903.reorderable.sample

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.reorderable.ReorderableItem
import io.github.halilozel1903.reorderable.core.ReorderAction
import io.github.halilozel1903.reorderable.core.move
import io.github.halilozel1903.reorderable.rememberReorderableLazyGridState
import io.github.halilozel1903.reorderable.rememberReorderableLazyListState
import kotlin.math.min

/** Lazy indices of the playlist: a "Now playing" label, the pinned track, an "Up next" label, then the queue. */
private const val PlaylistLeadingItems = 2

/**
 * The playlist: a `LazyColumn` with drag handles. The playing track is pinned: it is a disabled
 * `ReorderableItem` and locked with `isLocked`, so nothing can be moved above it.
 */
@Composable
fun PlaylistPane(trackIds: SnapshotStateList<String>, scene: Scene?, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = listState,
        // The two labels and the playing track.
        isLocked = { index -> index <= PlaylistLeadingItems },
    ) { from, to ->
        // The queue starts at lazy index 3 and track index 1: subtract the two labels.
        trackIds.move(from - PlaylistLeadingItems, to - PlaylistLeadingItems)
    }
    val movingKey = reorderState.draggingKey ?: reorderState.keyboardReorderKey
    val spacing = 8.dp

    Column(modifier) {
        PaneHeader(
            title = "Playlist",
            description = "Drag a handle to reorder. The playing track stays on top.",
            moving = (movingKey as? String)?.let { SampleData.track(it).title },
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "label_playing") { SectionLabel("Now playing") }
                val playing = trackIds.first()
                item(key = playing) {
                    ReorderableItem(reorderState, key = playing, enabled = false, shape = TrackShape) { isDragging ->
                        TrackRow(
                            track = SampleData.track(playing),
                            isDragging = isDragging,
                            isKeyboardReordering = false,
                            pinned = true,
                            handle = Modifier,
                            handleInteractions = remember { MutableInteractionSource() },
                        )
                    }
                }
                item(key = "label_next") { SectionLabel("Up next") }
                items(trackIds.drop(1), key = { it }) { id ->
                    ReorderableItem(reorderState, key = id, shape = TrackShape) { isDragging ->
                        val interactions = remember { MutableInteractionSource() }
                        TrackRow(
                            track = SampleData.track(id),
                            isDragging = isDragging,
                            isKeyboardReordering = isKeyboardReordering,
                            pinned = false,
                            handle = Modifier.draggableHandle(interactionSource = interactions),
                            handleInteractions = interactions,
                        )
                    }
                }
            }
            if (reorderState.isKeyboardReordering) {
                KeyboardHint(Modifier.align(Alignment.BottomCenter).padding(16.dp))
            }
        }
    }

    // Screenshot scenes: CI cannot drag, so drive the state from code into a fixed mid-drag or keyboard state.
    val density = LocalDensity.current
    LaunchedEffect(scene) {
        when (scene) {
            Scene.List -> {
                val key = "t04"
                val item = awaitVisibleItem(listState, key)
                val spacingPx = with(density) { spacing.toPx() }
                // 1.4 rows down: past the next track's middle, so it moves up and leaves a gap above the lifted row.
                reorderState.dragInSteps(key, Offset(0f, (item.size + spacingPx) * 1.4f))
            }
            Scene.Dpad -> {
                val key = "t03"
                awaitVisibleItem(listState, key)
                settle()
                if (reorderState.startKeyboardReorder(key)) {
                    repeat(2) {
                        settle()
                        reorderState.move(key, ReorderAction.MoveDown)
                    }
                }
            }
            else -> Unit
        }
    }
}

/** The widgets grid: a `LazyVerticalGrid` with items of different widths, long press to drag. */
@Composable
fun WidgetsPane(widgetIds: SnapshotStateList<String>, columns: Int, scene: Scene?, modifier: Modifier = Modifier) {
    val gridState = rememberLazyGridState()
    val reorderState = rememberReorderableLazyGridState(
        lazyGridState = gridState,
        // The search bar is locked at the top.
        isLocked = { index -> widgetIds.getOrNull(index)?.let { SampleData.widget(it).locked } == true },
    ) { from, to ->
        widgetIds.move(from, to)
    }
    val movingKey = reorderState.draggingKey ?: reorderState.keyboardReorderKey

    Column(modifier) {
        PaneHeader(
            title = "Widgets",
            description = "Long press a widget to move it. Wide widgets reflow the grid.",
            moving = (movingKey as? String)?.let { SampleData.widget(it).name },
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            items(
                items = widgetIds,
                key = { it },
                span = { id -> GridItemSpan(min(SampleData.widget(id).span, maxLineSpan)) },
            ) { id ->
                val widget = SampleData.widget(id)
                ReorderableItem(reorderState, key = id, enabled = !widget.locked, shape = WidgetShape) { isDragging ->
                    WidgetCard(widget, isDragging, Modifier.longPressDraggableHandle())
                }
            }
        }
    }

    LaunchedEffect(scene) {
        if (scene == Scene.Grid) {
            // Notes (second in its row) dragged down into the next row and a little to the right: Battery's
            // spot becomes its gap while the card floats above it.
            val key = "w_notes"
            val item = awaitVisibleItem(gridState, key)
            reorderState.dragInSteps(key, Offset(item.size.width * 0.25f, item.size.height * 0.68f))
        }
    }
}

/** The dock: a `LazyRow`, long press to drag. */
@Composable
fun DockPane(dockIds: SnapshotStateList<String>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to -> dockIds.move(from, to) }
    val movingKey = reorderState.draggingKey ?: reorderState.keyboardReorderKey

    Column(modifier) {
        PaneHeader(
            title = "Dock",
            description = "Long press an app and slide it along the row.",
            moving = (movingKey as? String)?.let { SampleData.dockApp(it).name },
        )
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(dockIds, key = { it }) { id ->
                ReorderableItem(reorderState, key = id, shape = WidgetShape) { isDragging ->
                    DockIcon(SampleData.dockApp(id), isDragging, Modifier.longPressDraggableHandle())
                }
            }
        }
    }
}
