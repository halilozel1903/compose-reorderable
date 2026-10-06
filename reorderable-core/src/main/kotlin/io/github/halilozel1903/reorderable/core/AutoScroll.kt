package io.github.halilozel1903.reorderable.core

/**
 * Auto-scroll while an item is dragged near an edge of the viewport.
 *
 * The speed is zero at [threshold] pixels from the edge and grows with an ease-in (quadratic) curve to the
 * maximum speed at the edge, so a small move towards the edge scrolls slowly and holding the item at the edge
 * scrolls fast.
 */
public object AutoScroll {

    /**
     * Scroll speed in pixels per second (never negative) for an item [distanceToEdge] pixels away from an edge.
     * A negative distance (the item is past the edge) gives [maxSpeed].
     */
    public fun speed(distanceToEdge: Float, threshold: Float, maxSpeed: Float): Float {
        require(threshold > 0f) { "threshold must be positive, was $threshold" }
        require(maxSpeed >= 0f) { "maxSpeed must not be negative, was $maxSpeed" }
        if (distanceToEdge >= threshold) return 0f
        val t = (1f - distanceToEdge / threshold).coerceIn(0f, 1f)
        return maxSpeed * easeIn(t)
    }

    /**
     * Signed scroll velocity in pixels per second for a dragged item from [itemStart] to [itemEnd] in a viewport
     * from [viewportStart] to [viewportEnd]: positive scrolls towards the end of the list, negative towards the
     * start, zero when the item is away from both edges (or close to both, when it is as large as the viewport).
     */
    public fun velocity(
        itemStart: Float,
        itemEnd: Float,
        viewportStart: Float,
        viewportEnd: Float,
        threshold: Float,
        maxSpeed: Float,
        canScrollBackward: Boolean = true,
        canScrollForward: Boolean = true,
    ): Float {
        val backward = if (canScrollBackward) speed(itemStart - viewportStart, threshold, maxSpeed) else 0f
        val forward = if (canScrollForward) speed(viewportEnd - itemEnd, threshold, maxSpeed) else 0f
        return forward - backward
    }

    /**
     * Pixels to scroll in one frame of [frameNanos] at [velocity] pixels per second. Long frames (a stall or the
     * first frame) count as at most [maxFrameNanos] so the list never jumps.
     */
    public fun frameDelta(velocity: Float, frameNanos: Long, maxFrameNanos: Long = 50_000_000L): Float {
        val nanos = frameNanos.coerceIn(0L, maxFrameNanos)
        return velocity * nanos / 1_000_000_000f
    }

    /** The ease-in curve: 0 at 0, 1 at 1, slow at the start. */
    public fun easeIn(t: Float): Float {
        val clamped = t.coerceIn(0f, 1f)
        return clamped * clamped
    }
}
