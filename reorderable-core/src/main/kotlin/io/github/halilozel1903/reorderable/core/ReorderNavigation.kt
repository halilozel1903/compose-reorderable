package io.github.halilozel1903.reorderable.core

/**
 * Where keyboard, D-pad and accessibility moves take an item.
 *
 * - Vertical list: up and down move one step, "Move to top" and "Move to bottom" go to the ends.
 * - Horizontal list: left and right move one step (mirrored in right-to-left layouts), "Move to start" and
 *   "Move to end" go to the ends.
 * - Grid (row by row): left and right move within the row, up and down move a whole row ([ReorderLayout.columns]
 *   items); down from the row above a shorter last row goes to the last item. "Move to top" and "Move to bottom"
 *   go to the first and last index.
 *
 * Locked items are respected (see [ReorderRules]): a step that would pass a locked item is not possible, and a
 * move to an end stops before the first locked item on the way.
 */
public object ReorderNavigation {

    /**
     * The index [action] moves the item at [index] to, or `null` when that move is not possible (not available
     * for the layout, at an edge, or blocked by a locked item).
     */
    public fun target(
        action: ReorderAction,
        index: Int,
        itemCount: Int,
        layout: ReorderLayout,
        isRtl: Boolean = false,
        isLocked: (index: Int) -> Boolean = { false },
    ): Int? {
        if (index !in 0 until itemCount) return null
        val raw = rawTarget(action, index, itemCount, layout, isRtl) ?: return null
        if (raw == index || raw !in 0 until itemCount) return null
        val reachable = ReorderRules.reachableTarget(index, raw, isLocked)
        return when {
            reachable == index -> null
            action.isStep && reachable != raw -> null
            else -> reachable
        }
    }

    /**
     * The actions possible for the item at [index], in reading order ("Move to top", "Move up", "Move left",
     * "Move right", "Move down", "Move to bottom"). A move to an end is left out when a one step move already
     * goes to the same index.
     */
    public fun availableActions(
        index: Int,
        itemCount: Int,
        layout: ReorderLayout,
        isRtl: Boolean = false,
        isLocked: (index: Int) -> Boolean = { false },
    ): List<ReorderActionTarget> {
        val order = when (layout.kind) {
            ReorderLayout.Kind.VerticalList -> listOf(
                ReorderAction.MoveToTop, ReorderAction.MoveUp, ReorderAction.MoveDown, ReorderAction.MoveToBottom,
            )
            ReorderLayout.Kind.HorizontalList -> listOf(
                ReorderAction.MoveToStart, ReorderAction.MoveLeft, ReorderAction.MoveRight, ReorderAction.MoveToEnd,
            )
            ReorderLayout.Kind.Grid -> listOf(
                ReorderAction.MoveToTop, ReorderAction.MoveUp, ReorderAction.MoveLeft,
                ReorderAction.MoveRight, ReorderAction.MoveDown, ReorderAction.MoveToBottom,
            )
        }
        val targets = order.mapNotNull { action ->
            target(action, index, itemCount, layout, isRtl, isLocked)?.let { ReorderActionTarget(action, it) }
        }
        val stepTargets = targets.filter { it.action.isStep }.map { it.targetIndex }.toSet()
        return targets.filter { it.action.isStep || it.targetIndex !in stepTargets }
    }

    private fun rawTarget(action: ReorderAction, index: Int, count: Int, layout: ReorderLayout, isRtl: Boolean): Int? {
        val last = count - 1
        return when (layout.kind) {
            ReorderLayout.Kind.VerticalList -> when (action) {
                ReorderAction.MoveUp -> index - 1
                ReorderAction.MoveDown -> index + 1
                ReorderAction.MoveToTop -> 0
                ReorderAction.MoveToBottom -> last
                else -> null
            }
            ReorderLayout.Kind.HorizontalList -> when (action) {
                ReorderAction.MoveLeft -> if (isRtl) index + 1 else index - 1
                ReorderAction.MoveRight -> if (isRtl) index - 1 else index + 1
                ReorderAction.MoveToStart -> 0
                ReorderAction.MoveToEnd -> last
                else -> null
            }
            ReorderLayout.Kind.Grid -> {
                val columns = layout.columns
                val column = index % columns
                val towardsStart = column > 0
                val towardsEnd = column < columns - 1 && index < last
                when (action) {
                    ReorderAction.MoveUp -> (index - columns).takeIf { it >= 0 }
                    ReorderAction.MoveDown -> when {
                        index + columns <= last -> index + columns
                        index / columns < last / columns -> last
                        else -> null
                    }
                    ReorderAction.MoveLeft -> if (isRtl) (index + 1).takeIf { towardsEnd } else (index - 1).takeIf { towardsStart }
                    ReorderAction.MoveRight -> if (isRtl) (index - 1).takeIf { towardsStart } else (index + 1).takeIf { towardsEnd }
                    ReorderAction.MoveToTop -> 0
                    ReorderAction.MoveToBottom -> last
                    else -> null
                }
            }
        }
    }
}
