package io.github.halilozel1903.reorderable.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** A violet and coral theme for Home Editor. */
@Composable
fun SampleTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) {
        darkColorScheme(
            primary = Color(0xFFC9BFFF),
            onPrimary = Color(0xFF2B1C84),
            primaryContainer = Color(0xFF42349C),
            onPrimaryContainer = Color(0xFFE6DEFF),
            secondary = Color(0xFFC9C3DC),
            secondaryContainer = Color(0xFF484459),
            onSecondaryContainer = Color(0xFFE5DFF9),
            tertiary = Color(0xFFFFB59F),
            tertiaryContainer = Color(0xFF8A321B),
            onTertiaryContainer = Color(0xFFFFDBD0),
            background = Color(0xFF131218),
            onBackground = Color(0xFFE5E1EA),
            surface = Color(0xFF131218),
            onSurface = Color(0xFFE5E1EA),
            surfaceVariant = Color(0xFF48454F),
            onSurfaceVariant = Color(0xFFC9C4D0),
            surfaceContainerLowest = Color(0xFF0E0D13),
            surfaceContainerLow = Color(0xFF1C1B21),
            surfaceContainer = Color(0xFF201F26),
            surfaceContainerHigh = Color(0xFF2B2931),
            surfaceContainerHighest = Color(0xFF36343C),
            outline = Color(0xFF938F99),
            outlineVariant = Color(0xFF48454F),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF5A4BC2),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE6DEFF),
            onPrimaryContainer = Color(0xFF170B5E),
            secondary = Color(0xFF5F5C71),
            secondaryContainer = Color(0xFFE5DFF9),
            onSecondaryContainer = Color(0xFF1C192B),
            tertiary = Color(0xFFB0462C),
            tertiaryContainer = Color(0xFFFFDBD0),
            onTertiaryContainer = Color(0xFF3B0900),
            background = Color(0xFFF7F5FC),
            onBackground = Color(0xFF1C1B21),
            surface = Color(0xFFF7F5FC),
            onSurface = Color(0xFF1C1B21),
            surfaceVariant = Color(0xFFE5E0EC),
            onSurfaceVariant = Color(0xFF48454F),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFFFFFF),
            surfaceContainer = Color(0xFFF0EDF7),
            surfaceContainerHigh = Color(0xFFEAE6F3),
            surfaceContainerHighest = Color(0xFFE4E0ED),
            outline = Color(0xFF79757F),
            outlineVariant = Color(0xFFCAC4D0),
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
