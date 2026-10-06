package io.github.halilozel1903.reorderable

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import io.github.halilozel1903.reorderable.core.ReorderAction

/** How many times a held key has repeated: 0 for the first key down. */
internal val KeyEvent.repeatCount: Int get() = nativeKeyEvent.repeatCount

/** Keys that pick up and drop an item in keyboard reorder mode. */
internal val ConfirmKeys: Set<Key> = setOf(Key.Enter, Key.NumPadEnter, Key.DirectionCenter, Key.Spacebar)

/** Arrow keys and the D-pad as one step moves. */
internal fun Key.toStepAction(): ReorderAction? = when (this) {
    Key.DirectionUp -> ReorderAction.MoveUp
    Key.DirectionDown -> ReorderAction.MoveDown
    Key.DirectionLeft -> ReorderAction.MoveLeft
    Key.DirectionRight -> ReorderAction.MoveRight
    else -> null
}
