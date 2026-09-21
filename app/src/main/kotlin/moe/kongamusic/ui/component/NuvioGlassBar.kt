/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.component

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.roundToInt

/**
 * Liquid Glass surface ported from NuvioMobile (NuvioMedia, GPL-3.0)
 * https://github.com/NuvioMedia/NuvioMobile — composeApp/src/androidMain/.../core/ui/glass/GlassBarSurface.kt
 *
 * AGSL runtime-shader refraction of the content behind the bar on Android 13+,
 * with a flat translucent capsule + edge highlight below that. The backdrop
 * sampler is fed by a Haze [HazeState] (the app wraps its screen content in a
 * [dev.chrisbanes.haze.hazeSource] for the NUVIO_GLASS bar) so the refraction
 * reflects real content scrolling underneath.
 */
private val NuvioGlassSurfaceColor = Color(0xFF1C1C1E)

private const val NuvioGlassOutsetDp = 24

internal const val NuvioGlassShader = """
uniform shader backdrop;
uniform float2 resolution;
uniform float density;
uniform float outset;
uniform float glowStrength;

half3 sampleLight(float2 position, float2 tangent) {
    float2 spread = tangent * density * 10.0;
    return backdrop.eval(position).rgb * 0.5
        + backdrop.eval(position - spread).rgb * 0.25
        + backdrop.eval(position + spread).rgb * 0.25;
}

half4 main(float2 position) {
    float2 halfSize = resolution * 0.5 - outset;
    float radius = halfSize.y;
    float2 local = position - resolution * 0.5;
    float2 capsule = float2(max(abs(local.x) - halfSize.x + radius, 0.0), local.y);
    float distanceToCenter = length(capsule);
    float distanceToEdge = distanceToCenter - radius;
    float coverage = 1.0 - smoothstep(-0.5, 0.5, distanceToEdge);
    if (coverage <= 0.0) return half4(0.0);

    float2 normal = float2(capsule.x * sign(local.x), capsule.y)
        / max(distanceToCenter, 0.001);
    float2 tangent = float2(-normal.y, normal.x);
    float depth = max(-distanceToEdge, 0.0) / density;
    half3 surface = mix(backdrop.eval(position).rgb, half3(28.0, 28.0, 30.0) / 255.0, 0.55);
    if (depth >= 16.0 || glowStrength <= 0.0) return half4(surface * coverage, coverage);

    float rim = exp(-0.0565 * depth - 0.0322 * depth * depth);
    float upperLight = 0.18 + 0.82 * pow(max(-normal.y, 0.0), 0.65);
    float bend = pow(rim, 0.18);

    half3 redLight = sampleLight(position - normal * density * 16.0 * bend, tangent);
    half3 greenLight = sampleLight(position - normal * density * 62.0 * bend, tangent);
    half3 blueLight = sampleLight(position - normal * density * 57.0 * bend, tangent);
    half3 refracted = half3(redLight.r, greenLight.g, blueLight.b);
    half luminance = dot(refracted, half3(0.2126, 0.7152, 0.0722));
    refracted = clamp(mix(half3(luminance), refracted, 1.25), 0.0, 1.0);

    half3 sheen = half3(0.1735, 0.0529, 0.0184) + refracted * half3(0.0953, 0.3152, 0.3822);
    half3 color = surface + sheen * rim * upperLight * glowStrength;
    float highlight = exp(-pow((depth - 0.35) / 0.42, 2.0));
    float highlightLight = 0.12 + 0.88 * sqrt(max((1.0 - normal.y) * 0.5, 0.0));
    color += half3(0.25) * highlight * highlightLight * glowStrength;
    return half4(clamp(color, 0.0, 1.0) * coverage, coverage);
}
"""

@Composable
internal fun NuvioGlassSurface(
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    glowStrength: Float = 1f,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        hazeState?.blurEnabled == true &&
        glowStrength > 0f
    ) {
        RefractedNuvioGlassSurface(
            hazeState = hazeState,
            modifier = modifier,
            glowStrength = glowStrength,
        )
    } else {
        Box(
            modifier
                .then(if (hazeState != null) Modifier.barBackdrop(hazeState) else Modifier)
                .drawWithCache {
                    val fill = NuvioGlassSurfaceColor.copy(alpha = if (hazeState != null) 0.55f else 0.82f)
                    val edge =
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.27f), Color.White.copy(alpha = 0.02f)),
                        )
                    val width = 0.75.dp.toPx()
                    onDrawBehind {
                        drawRect(fill)
                        drawRoundRect(
                            brush = edge,
                            topLeft = Offset(width / 2, width / 2),
                            size = Size(size.width - width, size.height - width),
                            cornerRadius = CornerRadius((size.height - width) / 2),
                            style = Stroke(width),
                            alpha = glowStrength,
                        )
                    }
                },
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun RefractedNuvioGlassSurface(
    hazeState: HazeState,
    modifier: Modifier,
    glowStrength: Float,
) {
    val shader = remember { RuntimeShader(NuvioGlassShader) }
    Box(
        modifier
            .layout { measurable, constraints ->
                val outset = NuvioGlassOutsetDp.dp.toPx().roundToInt()
                val placeable = measurable.measure(constraints.offset(outset * 2, outset * 2))
                layout(placeable.width - outset * 2, placeable.height - outset * 2) {
                    placeable.place(-outset, -outset)
                }
            }
            .graphicsLayer {
                shader.setFloatUniform("resolution", size.width.toFloat(), size.height.toFloat())
                shader.setFloatUniform("density", density)
                shader.setFloatUniform("outset", NuvioGlassOutsetDp.dp.toPx())
                shader.setFloatUniform("glowStrength", glowStrength)
                renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "backdrop").asComposeRenderEffect()
            }
            .barBackdrop(hazeState),
    )
}

private fun Modifier.barBackdrop(hazeState: HazeState): Modifier = hazeEffect(state = hazeState) {
    blurRadius = 24.dp
    backgroundColor = NuvioGlassSurfaceColor
    tints = listOf(HazeTint(Color.Transparent))
    noiseFactor = 0f
}