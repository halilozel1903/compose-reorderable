package io.github.halilozel1903.reorderable.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListMovesTest {

    private val letters = listOf("a", "b", "c", "d", "e")

    @Test
    fun movesForwardAndShiftsItemsInBetween() {
        assertEquals(listOf("a", "c", "d", "b", "e"), letters.moved(1, 3))
    }

    @Test
    fun movesBackward() {
        assertEquals(listOf("d", "a", "b", "c", "e"), letters.moved(3, 0))
    }

    @Test
    fun moveToSameIndexKeepsOrder() {
        assertEquals(letters, letters.moved(2, 2))
    }

    @Test
    fun moveIsReversible() {
        val list = letters.toMutableList()
        list.move(0, 4)
        assertEquals(listOf("b", "c", "d", "e", "a"), list)
        list.move(4, 0)
        assertEquals(letters, list)
    }

    @Test
    fun movedLeavesTheOriginalUntouched() {
        val original = letters.toList()
        letters.moved(0, 4)
        assertEquals(original, letters)
    }

    @Test
    fun rejectsIndicesOutsideTheList() {
        assertFailsWith<IndexOutOfBoundsException> { letters.moved(-1, 2) }
        assertFailsWith<IndexOutOfBoundsException> { letters.moved(0, 5) }
    }

    @Test
    fun movesByKey() {
        data class Song(val id: Int, val title: String)
        val songs = mutableListOf(Song(10, "Intro"), Song(20, "Verse"), Song(30, "Chorus"))
        assertTrue(songs.moveByKey(30, 10) { it.id })
        assertEquals(listOf(30, 10, 20), songs.map { it.id })
        assertFalse(songs.moveByKey(99, 10) { it.id })
        assertEquals(listOf(30, 10, 20), songs.map { it.id })
        assertEquals(listOf(10, 20, 30), songs.movedByKey(30, 20) { it.id }.map { it.id })
    }
}
