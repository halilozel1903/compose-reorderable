package io.github.halilozel1903.reorderable.core

/**
 * Rules for locked items: a locked item never moves, and no other item can be moved across it (moving an item
 * shifts every item in between, so passing a locked item would move it too).
 */
public object ReorderRules {

    /**
     * The index the item at [from] can actually move to when it is asked to go to [to]: [to] itself, or the last
     * index before the first locked item on the way. Returns [from] when that item is locked.
     */
    public fun reachableTarget(from: Int, to: Int, isLocked: (index: Int) -> Boolean): Int {
        if (from == to || isLocked(from)) return from
        var index = from
        if (to > from) {
            while (index < to && !isLocked(index + 1)) index++
        } else {
            while (index > to && !isLocked(index - 1)) index--
        }
        return index
    }

    /** `true` when the item at [from] can move all the way to [to]. */
    public fun canMove(from: Int, to: Int, isLocked: (index: Int) -> Boolean): Boolean =
        from != to && reachableTarget(from, to, isLocked) == to
}
