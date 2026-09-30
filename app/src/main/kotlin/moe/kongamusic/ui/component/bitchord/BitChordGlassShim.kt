/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Glass shim for the ported BitChord navigation components.
 *
 * BitChord's bar files call their own `Modifier.liquidGlass(shape)` and read a
 * handful of glass constants and helpers from their LiquidGlass.kt. KongaMusic
 * already has a liquid-glass implementation in ui/component/LiquidGlass.kt
 * (kyant0/backdrop, Apache-2.0) with a different signature: it takes the
 * Backdrop as a parameter rather than reading it from a CompositionLocal.
 *
 * Rather than vendor a second backdrop stack, this file satisfies the same
 * surface and delegates to the existing implementation, so there is one glass
 * implementation in the app and the ported bar files stay otherwise verbatim.
 */

package moe.kongamusic.ui.component.bitchord

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.kongamusic.ui.component.LocalLiquidGlassBackdrop
import moe.kongamusic.ui.component.liquidGlass as existingLiquidGlass
import moe.kongamusic.ui.component.liquidGlassContentColor

/** Whether the liquid glass nav bar is on. Mirrors BitChord's own local. */
val LocalLiquidGlassEnabled = staticCompositionLocalOf { false }

/** The backdrop path needs RenderEffect on a RenderNode: Android 12 (API 31) up. */
fun isGlassSupported(sdkInt: Int = Build.VERSION.SDK_INT): Boolean = sdkInt >= Build.VERSION_CODES.S

/** BitChord's page gutter, shared by the bar and the mini player. */
val PAGE_GUTTER: Dp = 10.dp

/** The hairline along a glass edge. */
val GLASS_EDGE_WIDTH: Dp = 0.5.dp
val GLASS_EDGE_COLOR: Color = Color.White.copy(alpha = 0.10f)

/** Content tint for text/icons on glass; inverse of the theme's luminance. */
@Composable
fun glassContentColor(): Color = liquidGlassContentColor()

/** Selected-tab indicator on glass, inverse of [glassContentColor]. */
@Composable
fun glassIndicatorColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) Color.White else Color.Black

/**
 * Glass surface for the ported bar components, delegating to the app's existing
 * implementation. Falls back to a plain translucent tint when glass is off or
 * unsupported, which is what BitChord's own call sites do by branching between
 * GlassNavBar and FloatingBottomBar.
 */
@Composable
fun Modifier.liquidGlass(shape: CornerBasedShape): Modifier {
    val enabled = LocalLiquidGlassEnabled.current && isGlassSupported()
    val backdrop = LocalLiquidGlassBackdrop.current
    if (enabled && backdrop != null) {
        return this.existingLiquidGlass(backdrop = backdrop, shape = shape)
    }
    // Glass unavailable: keep the translucent tint and the hairline so the
    // component still reads as a glass surface rather than a flat fill.
    val tint =
        if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) Color(0xFFFAFAFA) else Color(0xFF121212)
    return this
        .clip(shape)
        .background(tint.copy(alpha = 0.4f), shape)
        .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
}
