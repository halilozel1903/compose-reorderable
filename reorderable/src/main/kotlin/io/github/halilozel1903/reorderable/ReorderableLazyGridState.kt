package io.github.halilozel1903.reorderable

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope

/** Reordering state of a `LazyVerticalGrid` (or `LazyHorizontalGrid`), created with [rememberReorderableLazyGridState]. */
@Stable
public class ReorderableLazyGridState internal constructor(
    /** The grid's scroll state. */
    public val lazyGridState: LazyGridState,
    scope: CoroutineScope,
    environment: ReorderEnvironment,
) : ReorderableLazyState(LazyGridAdapter(lazyGridState), scope, environment)

/**
 * Creates a [ReorderableLazyGridState] for a `LazyVerticalGrid` that uses [lazyGridState]. Items may have
 * different spans and sizes; the item under the dragged item's center becomes the target once the dragged item
 * has passed its middle. Keyboard and accessibility moves go a column left or right and a row up or down.
 *
 * The parameters are the same as for [rememberReorderableLazyListState].
 */
@Composable
public fun rememberReorderableLazyGridState(
    lazyGridState: LazyGridState,
    isLocked: (index: Int) -> Boolean = { false },
    autoScrollThreshold: Dp = ReorderableDefaults.AutoScrollThreshold,
    autoScrollMaxSpeed: Dp = ReorderableDefaults.AutoScrollMaxSpeed,
    hapticFeedbackEnabled: Boolean = true,
    onDragStarted: (index: Int) -> Unit = {},
    onDragStopped: (from: Int, to: Int) -> Unit = { _, _ -> },
    onMove: (from: Int, to: Int) -> Unit,
): ReorderableLazyGridState {
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
    return remember(lazyGridState, scope, environment) {
        ReorderableLazyGridState(lazyGridState, scope, environment)
    }
}
