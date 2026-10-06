package io.github.halilozel1903.reorderable.sample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

val TrackShape = RoundedCornerShape(18.dp)
val WidgetShape = RoundedCornerShape(24.dp)

/** Title, description and, while an item moves, a "Moving ..." chip. */
@Composable
fun PaneHeader(title: String, description: String, moving: String?, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (moving != null) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        text = "Moving $moving",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
    )
}

/** A colored square with the first letter, standing in for album art and app icons. */
@Composable
fun Monogram(text: String, color: Color, size: Int, shapeRadius: Int, modifier: Modifier = Modifier) {
    val onColor = if (color.luminance() > 0.5f) Color(0xFF1C1B21) else Color.White
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(shapeRadius.dp))
            .background(color),
    ) {
        Text(
            text = text.take(1),
            color = onColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * A playlist row. [handle] is the library's drag handle modifier; [handleInteractions] shows its focus.
 * Keyboard reorder mode draws a thick primary outline, the focus highlight on TV.
 */
@Composable
fun TrackRow(
    track: Track,
    isDragging: Boolean,
    isKeyboardReordering: Boolean,
    pinned: Boolean,
    handle: Modifier,
    handleInteractions: MutableInteractionSource,
) {
    val colors = MaterialTheme.colorScheme
    val handleFocused by handleInteractions.collectIsFocusedAsState()
    Surface(
        shape = TrackShape,
        color = when {
            isDragging -> colors.surfaceContainerHighest
            pinned -> colors.primaryContainer
            else -> colors.surfaceContainerLow
        },
        border = when {
            isKeyboardReordering -> BorderStroke(3.dp, colors.primary)
            handleFocused -> BorderStroke(2.dp, colors.outline)
            else -> null
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
        ) {
            Monogram(track.title, track.color, size = 48, shapeRadius = 12)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (pinned) "Now playing · ${track.artist}" else "${track.artist} · ${track.duration}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (pinned) {
                Icon(
                    imageVector = SampleIcons.Lock,
                    contentDescription = "Pinned",
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp).size(22.dp),
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = handle
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isKeyboardReordering) colors.primary else Color.Transparent),
                ) {
                    Icon(
                        imageVector = SampleIcons.DragHandle,
                        contentDescription = "Reorder ${track.title}",
                        tint = if (isKeyboardReordering) colors.onPrimary else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** A home screen widget card. */
@Composable
fun WidgetCard(widget: HomeWidget, isDragging: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = WidgetShape,
        color = if (isDragging) colors.surfaceContainerHighest else colors.surfaceContainerLow,
        border = if (widget.locked) BorderStroke(1.dp, colors.outlineVariant) else null,
        modifier = modifier.fillMaxWidth().height(if (widget.locked) 72.dp else 128.dp),
    ) {
        if (widget.locked) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxHeight(),
            ) {
                Icon(SampleIcons.Search, contentDescription = null, tint = colors.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = widget.detail,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(SampleIcons.Lock, contentDescription = "Locked", tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        } else {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp).fillMaxHeight(),
            ) {
                Monogram(widget.name, widget.color, size = 36, shapeRadius = 18)
                Column {
                    Text(
                        text = widget.detail,
                        style = if (widget.span > 1) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = widget.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** An app in the dock. */
@Composable
fun DockIcon(app: DockApp, isDragging: Boolean, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(76.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDragging) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent)
            .padding(vertical = 8.dp),
    ) {
        Monogram(app.name, app.color, size = 52, shapeRadius = 26)
        Spacer(Modifier.height(6.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Shown while the playlist is in keyboard reorder mode. */
@Composable
fun KeyboardHint(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Icon(SampleIcons.Keyboard, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Arrow keys or D-pad to move · Enter to drop · Esc to cancel",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
