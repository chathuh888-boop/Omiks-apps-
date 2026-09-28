package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.LightBackground

@Composable
fun AmbientLiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Smooth subtle liquid breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidAnimation")
    val liquidPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) DarkBackground else LightBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (isDark) {
                // Dark Mode Luminous Liquid Blobs
                // Blob 1: Cyan/Azure Top-Right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3806B6D4), // Cyan
                            Color(0x180EA5E9),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.8f + liquidPulse * 0.1f), height * (0.15f - liquidPulse * 0.05f)),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.75f
                )

                // Blob 2: Violet/Purple Center-Left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x308B5CF6), // Violet
                            Color(0x156366F1),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.1f - liquidPulse * 0.08f), height * (0.45f + liquidPulse * 0.08f)),
                        radius = width * 0.85f
                    ),
                    center = Offset(width * 0.1f, height * 0.5f),
                    radius = width * 0.85f
                )

                // Blob 3: Deep Sky/Indigo Bottom-Center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2838BDF8),
                            Color(0x101E3A8A),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.6f - liquidPulse * 0.1f), height * (0.85f - liquidPulse * 0.05f)),
                        radius = width * 0.8f
                    ),
                    center = Offset(width * 0.6f, height * 0.85f),
                    radius = width * 0.8f
                )
            } else {
                // Light Mode Airy Prismatic Liquid Gradients
                // Blob 1: Soft Cyan Top-Right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3D38BDF8),
                            Color(0x14BAE6FD),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.85f - liquidPulse * 0.08f), height * (0.12f + liquidPulse * 0.05f)),
                        radius = width * 0.7f
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.7f
                )

                // Blob 2: Pastel Violet Center-Left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x32A78BFA),
                            Color(0x12DDD6FE),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.15f + liquidPulse * 0.08f), height * (0.48f - liquidPulse * 0.06f)),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.15f, height * 0.48f),
                    radius = width * 0.75f
                )

                // Blob 3: Soft Amber/Pink Bottom-Right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x28F472B6),
                            Color(0x0CFCE7F3),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.7f, height * 0.82f),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.7f, height * 0.82f),
                    radius = width * 0.75f
                )
            }
        }

        content()
    }
}

/**
 * Creates a translucent Liquid Glass surface modifier with specular glossy border sheen.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    isDark: Boolean = false,
    elevation: Dp = 4.dp,
    intensity: Float = 1.0f
): Modifier {
    val glassFill = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xB2131D33), // Translucent deep slate
                Color(0x8C0D1526)
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 600f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xEEFFFFFF), // Crisp translucent white
                Color(0xC8F4F8FC)
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 600f)
        )
    }

    val specularBorder = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x6638BDF8), // Glowing cyan specular rim at top-left
                Color(0x24818CF8),
                Color(0x10FFFFFF),
                Color(0x08FFFFFF)
            ),
            start = Offset(0f, 0f),
            end = Offset(300f, 300f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xE6FFFFFF), // Bright specular glossy highlight
                Color(0x70FFFFFF),
                Color(0x300284C7),
                Color(0x100284C7)
            ),
            start = Offset(0f, 0f),
            end = Offset(300f, 300f)
        )
    }

    return this
        .shadow(elevation, shape = shape, clip = false)
        .clip(shape)
        .background(glassFill, shape = shape)
        .border(width = (1.2f * intensity).dp, brush = specularBorder, shape = shape)
}

/**
 * Creates an ultra-glossy pill button modifier with glowing liquid refraction.
 */
fun Modifier.liquidGlassPill(
    shape: Shape = RoundedCornerShape(28.dp),
    isDark: Boolean = false
): Modifier {
    val pillFill = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xCC172545),
                Color(0x9E0F1A30)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xF5FFFFFF),
                Color(0xD9F0F7FF)
            )
        )
    }

    val borderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x8038BDF8),
                Color(0x20818CF8),
                Color(0x10FFFFFF)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0x80BAE6FD),
                Color(0x200284C7)
            )
        )
    }

    return this
        .shadow(6.dp, shape = shape)
        .clip(shape)
        .background(pillFill, shape = shape)
        .border(1.2.dp, borderBrush, shape)
}
