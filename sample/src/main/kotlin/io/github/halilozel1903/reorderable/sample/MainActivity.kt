package io.github.halilozel1903.reorderable.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * Home Editor, a fictional launcher's "edit home screen" page built with compose-reorderable: a playlist
 * (`LazyColumn` with drag handles), a widgets grid (`LazyVerticalGrid`, long press) and a dock (`LazyRow`, long
 * press).
 *
 * `scripts/screenshots.sh` starts it with `--es scene <scene>`. Emulators in CI cannot drag, so each scene drives
 * the reorder state from code into a fixed state:
 *
 * - `list`: the playlist mid-drag, a track lifted with a gap where it will land
 * - `grid`: the widgets grid mid-drag (tablets show the playlist, the grid and the dock together)
 * - `dpad`: the playlist in keyboard reorder mode, as with a TV remote or a hardware keyboard
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            SampleTheme(dark = isSystemInDarkTheme()) {
                // The Surface makes text default to onBackground, so it stays readable in dark mode.
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    HomeEditorApp(scene = scene)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}
