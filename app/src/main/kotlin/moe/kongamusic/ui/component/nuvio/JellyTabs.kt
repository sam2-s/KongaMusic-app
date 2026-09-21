/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Jelly tab row ported from NuvioMobile (NuvioMedia, GPL-3.0)
 * https://github.com/NuvioMedia/NuvioMobile — composeApp/src/androidMain/.../core/ui/jelly/JellyTabs.kt
 * Jelly physics derived from react-native-jelly-tabs (MIT), see NOTICE below.
 */

package moe.kongamusic.ui.component.nuvio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
internal fun JellyTabRow(
    items: List<JellyFloatingNavigationItem>,
    labelFraction: Float,
    motion: JellyMotion,
    active: Boolean,
    compactSize: Boolean,
    modifier: Modifier,
) {
    val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val iconSize = if (compactSize) 24.dp else 28.dp
    val labelHeight = if (compactSize) 14.dp else 16.dp
    val iconModifier = Modifier.size(iconSize)
        .then(if (active) Modifier.gradientMask(accentBrush()) else Modifier)
    val iconTint = if (active) Color.White else color
    Row(
        modifier = modifier.padding(4.dp).clearAndSetSemantics {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            Box(
                Modifier.weight(1f).fillMaxHeight().graphicsLayer {
                    val scale = if (active) motion.frame.contentScale else 1f
                    scaleX = scale
                    scaleY = scale
                },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(iconSize).graphicsLayer { translationY = 2.dp.toPx() * labelFraction }) {
                        Icon(
                            painter = painterResource(
                                if (active) item.iconIdActive else item.iconIdInactive,
                            ),
                            contentDescription = null,
                            modifier = iconModifier,
                            tint = iconTint,
                        )
                    }
                    Box(Modifier.height(labelHeight * labelFraction).fillMaxWidth().clipToBounds().alpha(labelFraction)) {
                        Text(
                            text = item.label,
                            color = color,
                            style = TextStyle(
                                fontSize = if (compactSize) 12.sp else 13.sp,
                                lineHeight = if (compactSize) 14.sp else 16.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun JellyTabTargets(
    items: List<JellyFloatingNavigationItem>,
    labelFraction: Float,
    motion: JellyMotion,
    compactSize: Boolean,
    modifier: Modifier,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(modifier.padding(horizontal = 4.dp).selectableGroup()) {
        items.forEachIndexed { index, item ->
            val visualIndex = visualNavIndex(index, items.size, isRtl)
            val onClick = {
                motion.select(visualIndex)
                item.onClick()
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .selectable(
                        selected = item.selected,
                        role = Role.Tab,
                        interactionSource = null,
                        indication = null,
                        onClick = onClick,
                    )
                    .clearAndSetSemantics {
                        role = Role.Tab
                        selected = item.selected
                        contentDescription = item.label
                        onClick { onClick(); true }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (item.content != null) {
                    Column(
                        modifier = Modifier.graphicsLayer {
                            val frame = motion.frame
                            val coverage = (1f - abs(frame.position - visualIndex)).coerceIn(0f, 1f)
                            val scale = 1f + (frame.contentScale - 1f) * coverage
                            scaleX = scale
                            scaleY = scale
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .then(if (compactSize) Modifier.size(24.dp) else Modifier)
                                .graphicsLayer { translationY = 2.dp.toPx() * labelFraction },
                        ) {
                            item.content(onClick)
                        }
                        Spacer(Modifier.height((if (compactSize) 14.dp else 16.dp) * labelFraction))
                    }
                }
            }
        }
    }
}

@Composable
private fun accentBrush(): Brush {
    val accent = MaterialTheme.colorScheme.primary
    val accentStrong = lerp(Color.White, accent, 0.35f)
    return Brush.linearGradient(listOf(accentStrong, accent))
}

private fun Modifier.gradientMask(brush: Brush): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(brush = brush, blendMode = BlendMode.SrcIn)
        }