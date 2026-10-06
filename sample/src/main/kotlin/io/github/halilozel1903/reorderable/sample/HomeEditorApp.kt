package io.github.halilozel1903.reorderable.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class EditorTab(val title: String) { Playlist("Playlist"), Widgets("Widgets"), Dock("Dock") }

/**
 * The home screen editor. Phones show one demo at a time with tabs; wide windows (tablets, TVs) show the
 * playlist and the dock next to the widgets grid.
 */
@Composable
fun HomeEditorApp(scene: Scene?) {
    val trackIds = rememberSaveable(saver = SampleData.IdListSaver) {
        SampleData.tracks.map { it.id }.toMutableStateList()
    }
    val widgetIds = rememberSaveable(saver = SampleData.IdListSaver) {
        SampleData.widgets.map { it.id }.toMutableStateList()
    }
    val dockIds = rememberSaveable(saver = SampleData.IdListSaver) {
        SampleData.dock.map { it.id }.toMutableStateList()
    }
    var tab by rememberSaveable {
        mutableStateOf(if (scene == Scene.Grid) EditorTab.Widgets else EditorTab.Playlist)
    }

    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
        val wide = maxWidth >= 840.dp
        val gridColumns = if (maxWidth >= 1200.dp) 4 else 3
        Column(Modifier.fillMaxSize()) {
            EditorTopBar()
            if (wide) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    // The playlist with the dock below it on the left, the widgets grid with the full height on the right.
                    Column(Modifier.width(400.dp).fillMaxHeight()) {
                        PlaylistPane(
                            trackIds = trackIds,
                            scene = scene.takeIf { it != Scene.Grid },
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                        DockPane(dockIds, Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    }
                    WidgetsPane(
                        widgetIds = widgetIds,
                        columns = gridColumns,
                        scene = scene,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            } else {
                TabSelector(selected = tab, onSelect = { tab = it })
                when (tab) {
                    EditorTab.Playlist -> PlaylistPane(trackIds, scene, Modifier.weight(1f).fillMaxWidth())
                    EditorTab.Widgets -> WidgetsPane(widgetIds, columns = 2, scene = scene, modifier = Modifier.weight(1f).fillMaxWidth())
                    EditorTab.Dock -> DockPane(dockIds, Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun EditorTopBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Home editor",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Arrange your playlist, widgets and dock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(SampleIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun TabSelector(selected: EditorTab, onSelect: (EditorTab) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        EditorTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Surface(
                onClick = { onSelect(tab) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                )
            }
        }
    }
}
