/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.player.tui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TuiKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bright: Boolean = false,
    big: Boolean = false,
    fill: Boolean = false,
    accent: Boolean = false,
    contentDescription: String = label,
) {
    var pressed by remember { mutableStateOf(false) }
    val bg =
        when {
            accent && bright -> TuiBright
            accent -> TuiRaised
            pressed -> TuiFg
            else -> Color.Transparent
        }
    val fg =
        when {
            accent && bright -> Color.Black
            accent -> TuiBright
            pressed -> Color.Black
            bright -> TuiBright
            else -> TuiFg
        }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .then(
                    if (fill) {
                        Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    } else {
                        Modifier.height(if (big) 44.dp else 34.dp)
                    },
                )
                .background(bg)
                .border(
                    BorderStroke(1.dp, if (accent && bright) TuiBright else TuiDim),
                    RectangleShape,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .semantics { this.contentDescription = contentDescription },
    ) {
        Text(
            text = label,
            color = fg,
            fontFamily = FontFamily.Monospace,
            fontSize = if (big) 17.sp else 11.sp,
            fontWeight = if (big) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
fun TuiStatus(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    on: Boolean = false,
    busy: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    indicator: String? = null,
) {
    val dot = if (busy) "◌" else if (on) "●" else "○"
    val dotColor = if (on) TuiGreen else TuiFaint
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier =
            modifier
                .clickable { onClick() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Text(
            text = if (indicator != null) "$indicator " else dot,
            color = dotColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
        )
        Text(
            text = "$label:",
            color = TuiDim,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
        )
        Text(
            text = value,
            color = if (on) TuiBright else TuiFg,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
        )
    }
}

@Composable
fun CursorTitle(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    color: Color = TuiBright,
) {
    val transition = rememberInfiniteTransition(label = "tuiCursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 600), RepeatMode.Reverse),
        label = "tuiCursorAlpha",
    )
    Box(modifier = modifier) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = maxLines,
            softWrap = false,
        )
        Text(
            text = "_",
            color = color.copy(alpha = alpha),
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun Hairline(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = TuiAccent,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(TuiLine),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(color),
        )
    }
}

@Composable
fun ThinSlider(
    fraction: Float,
    modifier: Modifier = Modifier,
    onScrub: (Float) -> Unit = {},
    onSeek: (Float) -> Unit = {},
) {
    val filled = fraction.coerceIn(0f, 1f)
    val filledGylphs = (filled * 40).toInt()
    val emptyGlyphs = 40 - filledGylphs
    val bar = "█".repeat(filledGylphs) + "░".repeat(emptyGlyphs)
    Box(
        modifier =
            modifier.pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek(offset.x / size.width)
                }
            },
    ) {
        Text(
            text = bar,
            color = TuiAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            letterSpacing = (-1).sp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun TuiPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .background(TuiRaised)
                .border(BorderStroke(1.dp, TuiLine), RectangleShape),
    ) {
        content()
    }
}

@Composable
fun TuiNotice(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(TuiSurface)
                .border(BorderStroke(1.dp, TuiDim), RectangleShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            color = TuiDim,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
        )
    }
}

@Composable
fun TuiChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(TuiSurface)
                .border(BorderStroke(1.dp, TuiLine), RectangleShape)
                .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            color = TuiDim,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
        )
    }
}

internal val TuiAccent: Color = Color(0xFFFFB000)