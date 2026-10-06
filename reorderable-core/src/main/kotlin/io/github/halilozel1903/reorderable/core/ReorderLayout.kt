package io.github.halilozel1903.reorderable.core

/**
 * How items are arranged on screen, which decides what "up", "down", "left" and "right" mean for keyboard,
 * D-pad and accessibility moves.
 */
public class ReorderLayout private constructor(
    /** The kind of layout. */
    public val kind: Kind,
    /** Items per row: 1 for a vertical list, the column count for a grid. Unused for a horizontal list. */
    public val columns: Int,
) {
    /** The kinds of layout. */
    public enum class Kind {
        /** A `LazyColumn`: up and down move the item. */
        VerticalList,

        /** A `LazyRow`: left and right move the item. */
        HorizontalList,

        /** A `LazyVerticalGrid` with items laid out row by row: all four directions move the item. */
        Grid,
    }

    override fun equals(other: Any?): Boolean =
        other is ReorderLayout && other.kind == kind && other.columns == columns

    override fun hashCode(): Int = 31 * kind.hashCode() + columns

    override fun toString(): String = when (kind) {
        Kind.VerticalList -> "ReorderLayout.VerticalList"
        Kind.HorizontalList -> "ReorderLayout.HorizontalList"
        Kind.Grid -> "ReorderLayout.grid($columns)"
    }

    public companion object {
        /** A vertical list. */
        public val VerticalList: ReorderLayout = ReorderLayout(Kind.VerticalList, 1)

        /** A horizontal list. */
        public val HorizontalList: ReorderLayout = ReorderLayout(Kind.HorizontalList, 1)

        /** A grid with [columns] items per row, laid out row by row. */
        public fun grid(columns: Int): ReorderLayout {
            require(columns >= 1) { "columns must be at least 1, was $columns" }
            return ReorderLayout(Kind.Grid, columns)
        }
    }
}
