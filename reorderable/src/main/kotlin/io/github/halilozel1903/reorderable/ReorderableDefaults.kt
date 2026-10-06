package io.github.halilozel1903.reorderable

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.reorderable.core.ReorderLabels

/** Default values of compose-reorderable. */
public object ReorderableDefaults {
    /** Auto-scroll starts when the dragged item is this close to an edge of the list. */
    public val AutoScrollThreshold: Dp = 64.dp

    /** Auto-scroll speed at the edge, per second. */
    public val AutoScrollMaxSpeed: Dp = 1600.dp

    /** Shadow elevation of a lifted item. */
    public val LiftedElevation: Dp = 8.dp

    /** Scale of a lifted item. */
    public const val LiftedScale: Float = 1.03f
}

/**
 * Labels of the accessibility actions ("Move up", "Move to top"...) and states. Provide translated
 * [ReorderLabels] here to localize every `ReorderableItem` below.
 */
public val LocalReorderLabels: ProvidableCompositionLocal<ReorderLabels> =
    staticCompositionLocalOf { ReorderLabels.English }
