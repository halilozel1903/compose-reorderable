package io.github.halilozel1903.reorderable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/** The latest callbacks and settings from composition, read by the state when it needs them. */
internal class ReorderEnvironment(
    val onMove: State<(from: Int, to: Int) -> Unit>,
    val isLocked: State<(index: Int) -> Boolean>,
    val onDragStarted: State<(index: Int) -> Unit>,
    val onDragStopped: State<(from: Int, to: Int) -> Unit>,
    val haptic: State<HapticFeedback?>,
    val autoScrollThreshold: State<Float>,
    val autoScrollMaxSpeed: State<Float>,
    val isRtl: State<Boolean>,
)

@Composable
internal fun rememberReorderEnvironment(
    onMove: (from: Int, to: Int) -> Unit,
    isLocked: (index: Int) -> Boolean,
    onDragStarted: (index: Int) -> Unit,
    onDragStopped: (from: Int, to: Int) -> Unit,
    hapticFeedbackEnabled: Boolean,
    autoScrollThreshold: Dp,
    autoScrollMaxSpeed: Dp,
): ReorderEnvironment {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val onMoveState = rememberUpdatedState(onMove)
    val isLockedState = rememberUpdatedState(isLocked)
    val onDragStartedState = rememberUpdatedState(onDragStarted)
    val onDragStoppedState = rememberUpdatedState(onDragStopped)
    val hapticState = rememberUpdatedState<HapticFeedback?>(if (hapticFeedbackEnabled) haptic else null)
    val thresholdState = rememberUpdatedState(with(density) { autoScrollThreshold.toPx() }.coerceAtLeast(1f))
    val maxSpeedState = rememberUpdatedState(with(density) { autoScrollMaxSpeed.toPx() }.coerceAtLeast(0f))
    val rtlState = rememberUpdatedState(LocalLayoutDirection.current == LayoutDirection.Rtl)
    return remember {
        ReorderEnvironment(
            onMove = onMoveState,
            isLocked = isLockedState,
            onDragStarted = onDragStartedState,
            onDragStopped = onDragStoppedState,
            haptic = hapticState,
            autoScrollThreshold = thresholdState,
            autoScrollMaxSpeed = maxSpeedState,
            isRtl = rtlState,
        )
    }
}
