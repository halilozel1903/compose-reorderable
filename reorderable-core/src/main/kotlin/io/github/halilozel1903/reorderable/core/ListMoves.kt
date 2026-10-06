package io.github.halilozel1903.reorderable.core

/**
 * Moves the item at [from] so that it ends up at index [to], shifting the items in between by one.
 *
 * This is the move `onMove` reports, so `items.move(from, to)` applies it to a `MutableList` or a
 * Compose `SnapshotStateList`.
 *
 * @throws IndexOutOfBoundsException when [from] or [to] is not an index of this list.
 */
public fun <T> MutableList<T>.move(from: Int, to: Int) {
    checkMoveIndex(from, size, "from")
    checkMoveIndex(to, size, "to")
    if (from == to) return
    add(to, removeAt(from))
}

/**
 * Returns a copy of this list with the item at [from] moved to index [to].
 *
 * @throws IndexOutOfBoundsException when [from] or [to] is not an index of this list.
 */
public fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    val copy = toMutableList()
    copy.move(from, to)
    return copy
}

/**
 * Moves the item whose key is [fromKey] to the position of the item whose key is [toKey].
 *
 * Returns `false` (and leaves the list unchanged) when either key is missing.
 */
public fun <T, K> MutableList<T>.moveByKey(fromKey: K, toKey: K, key: (T) -> K): Boolean {
    val from = indexOfFirst { key(it) == fromKey }
    val to = indexOfFirst { key(it) == toKey }
    if (from < 0 || to < 0) return false
    move(from, to)
    return true
}

/**
 * Returns a copy of this list with the item whose key is [fromKey] moved to the position of the item whose key
 * is [toKey], or an unchanged copy when either key is missing.
 */
public fun <T, K> List<T>.movedByKey(fromKey: K, toKey: K, key: (T) -> K): List<T> {
    val copy = toMutableList()
    copy.moveByKey(fromKey, toKey, key)
    return copy
}

private fun checkMoveIndex(index: Int, size: Int, name: String) {
    if (index !in 0 until size) throw IndexOutOfBoundsException("$name = $index is not an index of a list of size $size")
}
