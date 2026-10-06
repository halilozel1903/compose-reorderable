package io.github.halilozel1903.reorderable.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ReorderTargetsTest {

    // Five rows of 100 px, the slot of the dragged item (index 1) at 100..200.
    private val rows = (0 until 5).map { ItemSpan(it, it * 100f, 100f) }

    @Test
    fun staysUntilTheNextItemsCenterIsPassed() {
        assertEquals(1, ReorderTargets.listTarget(1, 100f + 49f, 100f, rows))
        assertEquals(2, ReorderTargets.listTarget(1, 100f + 51f, 100f, rows))
    }

    @Test
    fun jumpsSeveralItemsInOneFrame() {
        assertEquals(3, ReorderTargets.listTarget(1, 260f, 100f, rows))
        assertEquals(4, ReorderTargets.listTarget(1, 460f, 100f, rows))
    }

    @Test
    fun movesTowardsTheStart() {
        val slots = (0 until 5).map { ItemSpan(it, it * 100f, 100f) }
        assertEquals(3, ReorderTargets.listTarget(3, 260f, 100f, slots))
        assertEquals(2, ReorderTargets.listTarget(3, 240f, 100f, slots))
        assertEquals(0, ReorderTargets.listTarget(3, 10f, 100f, slots))
    }

    @Test
    fun differentSizesDoNotFlicker() {
        // A 20 px item dragged over a 200 px item.
        val before = listOf(ItemSpan(0, 0f, 20f), ItemSpan(1, 20f, 200f), ItemSpan(2, 220f, 50f))
        // The small item's end passes the large item's center at 120.
        assertEquals(0, ReorderTargets.listTarget(0, 99f, 20f, before))
        assertEquals(1, ReorderTargets.listTarget(0, 101f, 20f, before))
        // After the swap the large item is at 0..200 (center 100) and the slot at 200..220. The dragged item is
        // still drawn at 101, so its start edge has not passed the large item's center: no swap back.
        val after = listOf(ItemSpan(0, 0f, 200f), ItemSpan(1, 200f, 20f), ItemSpan(2, 220f, 50f))
        assertEquals(1, ReorderTargets.listTarget(1, 101f, 20f, after))
        assertEquals(0, ReorderTargets.listTarget(1, 99f, 20f, after))
    }

    @Test
    fun listRespectsLockedItems() {
        assertEquals(2, ReorderTargets.listTarget(1, 460f, 100f, rows) { it == 3 })
        assertEquals(1, ReorderTargets.listTarget(1, 160f, 100f, rows) { it == 2 })
    }

    @Test
    fun ignoresItemsThatAreNotVisible() {
        val visible = rows.filter { it.index <= 2 }
        assertEquals(2, ReorderTargets.listTarget(1, 460f, 100f, visible))
    }

    // A 3 column grid of 100 x 100 cells: 0 1 2 / 3 4 5 / 6 7.
    private val cells = (0 until 8).map { ItemRect(it, (it % 3) * 100f, (it / 3) * 100f, 100f, 100f) }

    private fun draggedAt(index: Int, dx: Float, dy: Float) = cells[index].offsetBy(dx, dy)

    @Test
    fun gridSwapsInARowAfterTheCenter() {
        assertEquals(0, ReorderTargets.gridTarget(0, draggedAt(0, 40f, 0f), cells))
        assertEquals(1, ReorderTargets.gridTarget(0, draggedAt(0, 60f, 0f), cells))
        assertEquals(2, ReorderTargets.gridTarget(0, draggedAt(0, 160f, 0f), cells))
    }

    @Test
    fun gridMovesBetweenRows() {
        assertEquals(1, ReorderTargets.gridTarget(1, draggedAt(1, 0f, 40f), cells))
        assertEquals(4, ReorderTargets.gridTarget(1, draggedAt(1, 0f, 60f), cells))
        assertEquals(7, ReorderTargets.gridTarget(1, draggedAt(1, 0f, 210f), cells))
        assertEquals(0, ReorderTargets.gridTarget(4, draggedAt(4, -100f, -60f), cells))
    }

    @Test
    fun gridKeepsTheTargetOverEmptySpace() {
        // Below the last row's free cell (index 8 does not exist).
        assertEquals(5, ReorderTargets.gridTarget(5, draggedAt(5, 0f, 100f), cells))
    }

    @Test
    fun gridWithDifferentSpans() {
        // 0 is two cells wide: [0 0 1] / [2 3 4].
        val spanned = listOf(
            ItemRect(0, 0f, 0f, 200f, 100f),
            ItemRect(1, 200f, 0f, 100f, 100f),
            ItemRect(2, 0f, 100f, 100f, 100f),
            ItemRect(3, 100f, 100f, 100f, 100f),
            ItemRect(4, 200f, 100f, 100f, 100f),
        )
        // Item 1 dragged left into the wide item: it must pass the wide item's center (x = 100).
        assertEquals(1, ReorderTargets.gridTarget(1, spanned[1].offsetBy(-80f, 0f), spanned))
        assertEquals(0, ReorderTargets.gridTarget(1, spanned[1].offsetBy(-160f, 0f), spanned))
        // Item 3 dragged up into the wide item.
        assertEquals(0, ReorderTargets.gridTarget(3, spanned[3].offsetBy(0f, -60f), spanned))
    }

    @Test
    fun gridRespectsLockedItems() {
        assertEquals(3, ReorderTargets.gridTarget(1, draggedAt(1, 0f, 210f), cells) { it == 4 })
        assertEquals(0, ReorderTargets.gridTarget(0, draggedAt(0, 60f, 0f), cells) { it == 0 })
    }
}
