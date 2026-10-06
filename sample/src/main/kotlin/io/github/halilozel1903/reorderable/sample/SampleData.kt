package io.github.halilozel1903.reorderable.sample

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color

/** A track of the playlist. All names are made up. */
data class Track(val id: String, val title: String, val artist: String, val duration: String, val color: Color)

/** A home screen widget; [span] is its width in grid cells. */
data class HomeWidget(
    val id: String,
    val name: String,
    val detail: String,
    val span: Int,
    val color: Color,
    val locked: Boolean = false,
)

/** An app in the dock. */
data class DockApp(val id: String, val name: String, val color: Color)

object SampleData {
    /** The first track is playing and pinned to the top. */
    val tracks = listOf(
        Track("t01", "Golden Hour Drive", "The Static Lanterns", "3:42", Color(0xFFF2994A)),
        Track("t02", "Paper Planets", "Mira Okafor", "4:05", Color(0xFF6C5CE7)),
        Track("t03", "Low Tide Radio", "Cobalt Avenue", "3:18", Color(0xFF00A8A8)),
        Track("t04", "Northbound", "Ellis and the Quiet", "5:01", Color(0xFFE84393)),
        Track("t05", "Velvet Static", "Juniper Ray", "2:57", Color(0xFF0984E3)),
        Track("t06", "Midnight Ferry", "Harbor Lights", "4:26", Color(0xFF2D3436)),
        Track("t07", "Sunny Arcade", "Pixel Parade", "3:33", Color(0xFFFDCB6E)),
        Track("t08", "Kites Over the Bay", "Paloma Vale", "3:49", Color(0xFF00B894)),
        Track("t09", "Small Hours", "The Night Owls Club", "3:50", Color(0xFF8E44AD)),
        Track("t10", "Echo Garden", "Lumen Fields", "4:12", Color(0xFFD35400)),
    )

    val widgets = listOf(
        HomeWidget("w_search", "Search", "Apps, contacts and the web", span = Int.MAX_VALUE, color = Color(0xFF5A4BC2), locked = true),
        HomeWidget("w_weather", "Weather", "21° · Sunny", span = 2, color = Color(0xFF0984E3)),
        HomeWidget("w_calendar", "Calendar", "Standup at 10:00", span = 2, color = Color(0xFFE17055)),
        HomeWidget("w_clock", "Clock", "09:41", span = 1, color = Color(0xFF2D3436)),
        HomeWidget("w_notes", "Notes", "3 lists", span = 1, color = Color(0xFFFDCB6E)),
        HomeWidget("w_photos", "Photos", "Memories", span = 1, color = Color(0xFFE84393)),
        HomeWidget("w_music", "Music", "Paused", span = 1, color = Color(0xFF6C5CE7)),
        HomeWidget("w_steps", "Steps", "6,204", span = 1, color = Color(0xFF00B894)),
        HomeWidget("w_battery", "Battery", "86%", span = 1, color = Color(0xFF55A630)),
        HomeWidget("w_timer", "Timer", "25:00", span = 1, color = Color(0xFFD35400)),
        HomeWidget("w_podcasts", "Podcasts", "2 new", span = 1, color = Color(0xFF8E44AD)),
        HomeWidget("w_home", "Smart home", "Lights on in 2 rooms", span = 2, color = Color(0xFF00A8A8)),
        HomeWidget("w_fitness", "Fitness", "Move ring 72%", span = 2, color = Color(0xFFC0392B)),
    )

    val dock = listOf(
        DockApp("d_phone", "Phone", Color(0xFF00B894)),
        DockApp("d_messages", "Messages", Color(0xFF0984E3)),
        DockApp("d_camera", "Camera", Color(0xFF2D3436)),
        DockApp("d_maps", "Maps", Color(0xFF55A630)),
        DockApp("d_browser", "Browser", Color(0xFFE17055)),
        DockApp("d_mail", "Mail", Color(0xFF5A4BC2)),
        DockApp("d_music", "Music", Color(0xFFE84393)),
        DockApp("d_notes", "Notes", Color(0xFFFDCB6E)),
    )

    private val tracksById = tracks.associateBy { it.id }
    private val widgetsById = widgets.associateBy { it.id }
    private val dockById = dock.associateBy { it.id }

    fun track(id: String): Track = tracksById.getValue(id)
    fun widget(id: String): HomeWidget = widgetsById.getValue(id)
    fun dockApp(id: String): DockApp = dockById.getValue(id)

    /** Keeps an order of ids across configuration changes and process death. */
    val IdListSaver: Saver<SnapshotStateList<String>, Any> = listSaver<SnapshotStateList<String>, String>(
        save = { it.toList() },
        restore = { it.toMutableStateList() },
    )
}
