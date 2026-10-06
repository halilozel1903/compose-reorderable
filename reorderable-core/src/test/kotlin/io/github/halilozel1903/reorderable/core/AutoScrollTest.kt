package io.github.halilozel1903.reorderable.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AutoScrollTest {

    @Test
    fun noSpeedOutsideTheThreshold() {
        assertEquals(0f, AutoScroll.speed(100f, threshold = 100f, maxSpeed = 2000f))
        assertEquals(0f, AutoScroll.speed(400f, threshold = 100f, maxSpeed = 2000f))
    }

    @Test
    fun maxSpeedAtOrPastTheEdge() {
        assertEquals(2000f, AutoScroll.speed(0f, threshold = 100f, maxSpeed = 2000f))
        assertEquals(2000f, AutoScroll.speed(-50f, threshold = 100f, maxSpeed = 2000f))
    }

    @Test
    fun easesIn() {
        // Half way into the threshold is a quarter of the speed, not half.
        assertEquals(500f, AutoScroll.speed(50f, threshold = 100f, maxSpeed = 2000f), 0.01f)
        val samples = (100 downTo 0 step 10).map { AutoScroll.speed(it.toFloat(), 100f, 2000f) }
        assertTrue(samples.zipWithNext().all { (a, b) -> b > a })
        val steps = samples.zipWithNext { a, b -> b - a }
        assertTrue(steps.zipWithNext().all { (a, b) -> b >= a }, "speed grows faster closer to the edge")
    }

    @Test
    fun velocityIsSignedByEdge() {
        // Viewport 0..1000, threshold 100.
        assertEquals(0f, AutoScroll.velocity(400f, 500f, 0f, 1000f, 100f, 2000f))
        assertTrue(AutoScroll.velocity(920f, 1020f, 0f, 1000f, 100f, 2000f) > 0f)
        assertTrue(AutoScroll.velocity(-10f, 90f, 0f, 1000f, 100f, 2000f) < 0f)
        assertEquals(0f, AutoScroll.velocity(-10f, 90f, 0f, 1000f, 100f, 2000f, canScrollBackward = false))
        assertEquals(0f, AutoScroll.velocity(920f, 1020f, 0f, 1000f, 100f, 2000f, canScrollForward = false))
    }

    @Test
    fun frameDeltaCapsLongFrames() {
        assertEquals(16f, AutoScroll.frameDelta(1000f, 16_000_000L), 0.001f)
        assertEquals(50f, AutoScroll.frameDelta(1000f, 2_000_000_000L), 0.001f)
        assertEquals(-8f, AutoScroll.frameDelta(-500f, 16_000_000L), 0.001f)
    }

    @Test
    fun rejectsBadArguments() {
        assertFailsWith<IllegalArgumentException> { AutoScroll.speed(10f, threshold = 0f, maxSpeed = 1f) }
        assertFailsWith<IllegalArgumentException> { AutoScroll.speed(10f, threshold = 10f, maxSpeed = -1f) }
    }
}
