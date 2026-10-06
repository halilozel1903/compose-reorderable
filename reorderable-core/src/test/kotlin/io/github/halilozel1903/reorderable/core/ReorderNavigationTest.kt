package io.github.halilozel1903.reorderable.core

import io.github.halilozel1903.reorderable.core.ReorderAction.MoveDown
import io.github.halilozel1903.reorderable.core.ReorderAction.MoveLeft
import io.github.halilozel1903.reorderable.core.ReorderAction.MoveRight
import io.github.halilozel1903.reorderable.core.ReorderAction.MoveToBottom
import io.github.halilozel1903.reorderable.core.ReorderAction.MoveToEnd
import io.github.halilozel1903.reorderable.core.ReorderAction.MoveToStart
import io.github.halilozel1903.reorderable.core.ReorderAction.MoveToTop
import io.github.halilozel1903.reorderable.core.ReorderAction.MoveUp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ReorderNavigationTest {

    private val column = ReorderLayout.VerticalList
    private val row = ReorderLayout.HorizontalList

    @Test
    fun verticalListStepsAndEnds() {
        assertEquals(1, ReorderNavigation.target(MoveUp, 2, 5, column))
        assertEquals(3, ReorderNavigation.target(MoveDown, 2, 5, column))
        assertEquals(0, ReorderNavigation.target(MoveToTop, 2, 5, column))
        assertEquals(4, ReorderNavigation.target(MoveToBottom, 2, 5, column))
        assertNull(ReorderNavigation.target(MoveUp, 0, 5, column))
        assertNull(ReorderNavigation.target(MoveDown, 4, 5, column))
        assertNull(ReorderNavigation.target(MoveLeft, 2, 5, column))
        assertNull(ReorderNavigation.target(MoveToStart, 2, 5, column))
    }

    @Test
    fun horizontalListMirrorsInRtl() {
        assertEquals(1, ReorderNavigation.target(MoveLeft, 2, 5, row))
        assertEquals(3, ReorderNavigation.target(MoveRight, 2, 5, row))
        assertEquals(3, ReorderNavigation.target(MoveLeft, 2, 5, row, isRtl = true))
        assertEquals(1, ReorderNavigation.target(MoveRight, 2, 5, row, isRtl = true))
        assertEquals(0, ReorderNavigation.target(MoveToStart, 2, 5, row))
        assertEquals(4, ReorderNavigation.target(MoveToEnd, 2, 5, row))
        assertNull(ReorderNavigation.target(MoveUp, 2, 5, row))
    }

    // 0 1 2 3 / 4 5 6 7 / 8 9
    private val grid = ReorderLayout.grid(4)

    @Test
    fun gridMovesByRowsAndColumns() {
        assertEquals(1, ReorderNavigation.target(MoveUp, 5, 10, grid))
        assertEquals(9, ReorderNavigation.target(MoveDown, 5, 10, grid))
        assertEquals(4, ReorderNavigation.target(MoveLeft, 5, 10, grid))
        assertEquals(6, ReorderNavigation.target(MoveRight, 5, 10, grid))
        assertNull(ReorderNavigation.target(MoveLeft, 4, 10, grid), "no wrapping to the previous row")
        assertNull(ReorderNavigation.target(MoveRight, 7, 10, grid), "no wrapping to the next row")
        assertNull(ReorderNavigation.target(MoveUp, 2, 10, grid))
        assertNull(ReorderNavigation.target(MoveRight, 9, 10, grid))
    }

    @Test
    fun gridDownIntoAShortLastRowGoesToTheLastItem() {
        assertEquals(9, ReorderNavigation.target(MoveDown, 7, 10, grid))
        assertNull(ReorderNavigation.target(MoveDown, 8, 10, grid))
    }

    @Test
    fun gridMirrorsLeftAndRightInRtl() {
        assertEquals(6, ReorderNavigation.target(MoveLeft, 5, 10, grid, isRtl = true))
        assertEquals(4, ReorderNavigation.target(MoveRight, 5, 10, grid, isRtl = true))
    }

    @Test
    fun stepsBlockedByLockedItems() {
        val pinnedFirst: (Int) -> Boolean = { it == 0 }
        assertNull(ReorderNavigation.target(MoveUp, 1, 5, column, isLocked = pinnedFirst))
        // "Move to top" stops below the pinned item.
        assertEquals(1, ReorderNavigation.target(MoveToTop, 3, 5, column, isLocked = pinnedFirst))
        // A row step in the grid passes the three items in between; one of them is locked.
        assertNull(ReorderNavigation.target(MoveDown, 1, 10, grid) { it == 3 })
        assertNull(ReorderNavigation.target(MoveDown, 0, 5, column) { it == 0 }, "locked items do not move")
    }

    @Test
    fun availableActionsInReadingOrder() {
        assertEquals(
            listOf(
                ReorderActionTarget(MoveToTop, 0),
                ReorderActionTarget(MoveUp, 2),
                ReorderActionTarget(MoveDown, 4),
                ReorderActionTarget(MoveToBottom, 5),
            ),
            ReorderNavigation.availableActions(3, 6, column),
        )
    }

    @Test
    fun availableActionsDropDuplicatesAndImpossibleMoves() {
        // Index 1: "Move to top" would be the same as "Move up".
        assertEquals(
            listOf(MoveUp, MoveDown, MoveToBottom),
            ReorderNavigation.availableActions(1, 5, column).map { it.action },
        )
        assertEquals(
            listOf(MoveRight, MoveToEnd),
            ReorderNavigation.availableActions(0, 5, row).map { it.action },
        )
        assertEquals(emptyList(), ReorderNavigation.availableActions(0, 1, column))
        assertEquals(
            listOf(MoveToTop, MoveUp, MoveLeft, MoveRight, MoveDown, MoveToBottom),
            ReorderNavigation.availableActions(5, 14, grid).map { it.action },
        )
    }

    @Test
    fun labels() {
        assertEquals("Move to top", ReorderLabels.English.labelFor(MoveToTop))
        assertEquals("Move right", ReorderLabels.English.labelFor(MoveRight))
        assertEquals("Position 3 of 8", ReorderLabels.English.position(2, 8))
        val turkish = ReorderLabels(moveUp = "Yukarı taşı", positionTemplate = "{count} öğeden {position}.")
        assertEquals("Yukarı taşı", turkish.labelFor(MoveUp))
        assertEquals("8 öğeden 3.", turkish.position(2, 8))
        assertEquals("Move down", turkish.labelFor(MoveDown))
    }

    @Test
    fun gridNeedsAtLeastOneColumn() {
        assertFailsWith<IllegalArgumentException> { ReorderLayout.grid(0) }
        assertEquals(ReorderLayout.grid(3), ReorderLayout.grid(3))
    }
}
