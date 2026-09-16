/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.player.tui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

@Immutable
data class TuiColors(
    val bg: Color = Color.Black,
    val surface: Color = Color(0xFF131415),
    val raised: Color = Color(0xFF080808),
    val fg: Color = Color(0xFFC6CBCC),
    val bright: Color = Color(0xFFDFE4E5),
    val dim: Color = Color(0xFF767D80),
    val faint: Color = Color(0xFF3C4245),
    val line: Color = Color(0xFF1E2122),
    val red: Color = Color(0xFFE05252),
    val green: Color = Color(0xFF5FBE6A),
)

val TuiBg: Color = TuiColors().bg
val TuiSurface: Color = TuiColors().surface
val TuiRaised: Color = TuiColors().raised
val TuiFg: Color = TuiColors().fg
val TuiBright: Color = TuiColors().bright
val TuiDim: Color = TuiColors().dim
val TuiFaint: Color = TuiColors().faint
val TuiLine: Color = TuiColors().line
val TuiRed: Color = TuiColors().red
val TuiGreen: Color = TuiColors().green

val LocalTuiColors = staticCompositionLocalOf { TuiColors() }

object TuiType {
    val Mono: FontFamily = FontFamily.Monospace
    val body = TextStyle(fontFamily = Mono, fontSize = 14.sp)
    val label = TextStyle(fontFamily = Mono, fontSize = 12.sp)
    val tiny = TextStyle(fontFamily = Mono, fontSize = 9.sp)
    val display = TextStyle(fontFamily = Mono, fontSize = 20.sp, letterSpacing = 1.sp)
}