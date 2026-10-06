package io.github.halilozel1903.reorderable.core

/** An item of a list along the list's main axis, in pixels. */
public data class ItemSpan(
    /** The item's index in the list. */
    public val index: Int,
    /** Offset of the item's start edge. */
    public val start: Float,
    /** Size of the item along the main axis. */
    public val size: Float,
) {
    /** Offset of the item's end edge. */
    public val end: Float get() = start + size

    /** Offset of the item's center. */
    public val center: Float get() = start + size / 2f
}

/** An item of a grid, in pixels. Rows go left to right (start to end) and top to bottom. */
public data class ItemRect(
    /** The item's index in the grid. */
    public val index: Int,
    public val left: Float,
    public val top: Float,
    public val width: Float,
    public val height: Float,
) {
    public val right: Float get() = left + width
    public val bottom: Float get() = top + height
    public val centerX: Float get() = left + width / 2f
    public val centerY: Float get() = top + height / 2f

    /** `true` when the point ([x], [y]) is inside this rectangle (the right and bottom edges excluded). */
    public fun contains(x: Float, y: Float): Boolean = x >= left && x < right && y >= top && y < bottom

    /** This rectangle moved by ([dx], [dy]). */
    public fun offsetBy(dx: Float, dy: Float): ItemRect = copy(left = left + dx, top = top + dy)
}
