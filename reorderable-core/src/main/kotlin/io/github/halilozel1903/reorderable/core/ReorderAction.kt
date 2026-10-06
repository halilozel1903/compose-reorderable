package io.github.halilozel1903.reorderable.core

/**
 * A move that does not need dragging: from the keyboard, a D-pad or an accessibility service.
 *
 * Left and right are physical directions; in a right-to-left layout "Move left" moves an item towards the end.
 */
public enum class ReorderAction(
    /** The English label, used by [ReorderLabels.English]. */
    public val defaultLabel: String,
) {
    MoveToTop("Move to top"),
    MoveUp("Move up"),
    MoveDown("Move down"),
    MoveToBottom("Move to bottom"),
    MoveToStart("Move to start"),
    MoveLeft("Move left"),
    MoveRight("Move right"),
    MoveToEnd("Move to end"),
    ;

    /** `true` for the one step moves (up, down, left and right), the moves arrow keys and the D-pad make. */
    public val isStep: Boolean
        get() = this == MoveUp || this == MoveDown || this == MoveLeft || this == MoveRight
}

/** An action that is possible for an item, with the index the item would move to. */
public data class ReorderActionTarget(
    public val action: ReorderAction,
    public val targetIndex: Int,
)

/**
 * Labels for [ReorderAction]s, shown by accessibility services such as TalkBack as custom actions. Pass
 * translated strings for other languages.
 */
public class ReorderLabels(
    public val moveToTop: String = ReorderAction.MoveToTop.defaultLabel,
    public val moveUp: String = ReorderAction.MoveUp.defaultLabel,
    public val moveDown: String = ReorderAction.MoveDown.defaultLabel,
    public val moveToBottom: String = ReorderAction.MoveToBottom.defaultLabel,
    public val moveToStart: String = ReorderAction.MoveToStart.defaultLabel,
    public val moveLeft: String = ReorderAction.MoveLeft.defaultLabel,
    public val moveRight: String = ReorderAction.MoveRight.defaultLabel,
    public val moveToEnd: String = ReorderAction.MoveToEnd.defaultLabel,
    /** State description of an item that is being moved. */
    public val reordering: String = "Reordering",
    /**
     * Position description; `{position}` (1 based) and `{count}` are replaced, see [position].
     */
    public val positionTemplate: String = "Position {position} of {count}",
) {
    /** The label of [action]. */
    public fun labelFor(action: ReorderAction): String = when (action) {
        ReorderAction.MoveToTop -> moveToTop
        ReorderAction.MoveUp -> moveUp
        ReorderAction.MoveDown -> moveDown
        ReorderAction.MoveToBottom -> moveToBottom
        ReorderAction.MoveToStart -> moveToStart
        ReorderAction.MoveLeft -> moveLeft
        ReorderAction.MoveRight -> moveRight
        ReorderAction.MoveToEnd -> moveToEnd
    }

    /** "Position 3 of 8" for the item at [index] (0 based) of [count] items. */
    public fun position(index: Int, count: Int): String =
        positionTemplate.replace("{position}", (index + 1).toString()).replace("{count}", count.toString())

    public companion object {
        /** English labels: "Move up", "Move to top" and so on. */
        public val English: ReorderLabels = ReorderLabels()
    }
}
