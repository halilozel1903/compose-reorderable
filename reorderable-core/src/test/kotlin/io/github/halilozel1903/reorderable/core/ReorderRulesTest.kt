package io.github.halilozel1903.reorderable.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReorderRulesTest {

    private val lockedAt = setOf(0, 4)
    private val isLocked: (Int) -> Boolean = { it in lockedAt }

    @Test
    fun unlockedPathReachesTarget() {
        assertEquals(3, ReorderRules.reachableTarget(1, 3, isLocked))
        assertEquals(1, ReorderRules.reachableTarget(3, 1, isLocked))
    }

    @Test
    fun stopsBeforeALockedItem() {
        assertEquals(3, ReorderRules.reachableTarget(2, 6, isLocked))
        assertEquals(1, ReorderRules.reachableTarget(3, 0, isLocked))
        assertEquals(5, ReorderRules.reachableTarget(7, 2, isLocked))
    }

    @Test
    fun lockedItemNeverMoves() {
        assertEquals(4, ReorderRules.reachableTarget(4, 1, isLocked))
        assertFalse(ReorderRules.canMove(0, 2, isLocked))
    }

    @Test
    fun canMoveOnlyWhenTheWholeWayIsFree() {
        assertTrue(ReorderRules.canMove(1, 3, isLocked))
        assertFalse(ReorderRules.canMove(1, 5, isLocked))
        assertFalse(ReorderRules.canMove(2, 2, isLocked))
    }
}
