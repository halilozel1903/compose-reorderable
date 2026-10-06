package io.github.halilozel1903.reorderable.core

/**
 * Finds the index a dragged item should move to from where it is drawn now.
 *
 * Both functions work with items of different sizes and avoid flickering back and forth: the dragged item takes
 * another item's place only once it has passed that item's center, so right after a swap it is never past the
 * center needed to swap back.
 */
public object ReorderTargets {

    /**
     * Drop target in a list (a column or a row).
     *
     * @param draggedIndex the dragged item's current index (where its slot, the gap, is now).
     * @param draggedStart where the dragged item is drawn now: its start edge along the main axis.
     * @param draggedSize the dragged item's size along the main axis.
     * @param items the visible items in the same coordinates; the dragged item itself is ignored.
     * @param isLocked items that cannot move or be passed, see [ReorderRules].
     * @return the new index, or [draggedIndex] when the item should stay.
     */
    public fun listTarget(
        draggedIndex: Int,
        draggedStart: Float,
        draggedSize: Float,
        items: List<ItemSpan>,
        isLocked: (index: Int) -> Boolean = { false },
    ): Int {
        val draggedEnd = draggedStart + draggedSize
        var target = draggedIndex
        // Moving towards the end: the furthest item whose center the dragged item's end edge has passed.
        for (item in items) {
            if (item.index > target && draggedEnd > item.center) target = item.index
        }
        if (target == draggedIndex) {
            // Moving towards the start: the furthest item whose center the dragged item's start edge has passed.
            for (item in items) {
                if (item.index < target && draggedStart < item.center) target = item.index
            }
        }
        return ReorderRules.reachableTarget(draggedIndex, target, isLocked)
    }

    /**
     * Drop target in a grid whose items are laid out row by row and may have different sizes (spans).
     *
     * The dragged item's center picks the item under it. That item becomes the target once the dragged item's
     * leading edge has also passed its middle: horizontally when it is in the same row as the dragged item's slot,
     * vertically when it is in another row.
     *
     * @param draggedIndex the dragged item's current index (where its slot, the gap, is now).
     * @param dragged where the dragged item is drawn now.
     * @param items the visible items in the same coordinates, including the dragged item's slot if visible.
     * @param isLocked items that cannot move or be passed, see [ReorderRules].
     * @return the new index, or [draggedIndex] when the item should stay.
     */
    public fun gridTarget(
        draggedIndex: Int,
        dragged: ItemRect,
        items: List<ItemRect>,
        isLocked: (index: Int) -> Boolean = { false },
    ): Int {
        val x = dragged.centerX
        val y = dragged.centerY
        val slot = items.firstOrNull { it.index == draggedIndex }
        val candidate = items.firstOrNull { it.index != draggedIndex && it.contains(x, y) } ?: return draggedIndex
        if (slot != null) {
            val sameRow = candidate.top < slot.bottom && slot.top < candidate.bottom
            val passed = if (sameRow) {
                if (candidate.centerX >= slot.centerX) dragged.right > candidate.centerX else dragged.left < candidate.centerX
            } else {
                if (candidate.centerY >= slot.centerY) dragged.bottom > candidate.centerY else dragged.top < candidate.centerY
            }
            if (!passed) return draggedIndex
        }
        return ReorderRules.reachableTarget(draggedIndex, candidate.index, isLocked)
    }
}
