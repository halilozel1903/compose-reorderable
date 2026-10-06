package io.github.halilozel1903.reorderable.sample

/** Screenshot scenes, picked with the `scene` intent extra. */
enum class Scene(val key: String) {
    /** The playlist mid-drag. */
    List("list"),

    /** The widgets grid mid-drag. */
    Grid("grid"),

    /** The playlist in keyboard (D-pad) reorder mode. */
    Dpad("dpad"),
    ;

    companion object {
        fun from(key: String?): Scene? = entries.firstOrNull { it.key == key }
    }
}
