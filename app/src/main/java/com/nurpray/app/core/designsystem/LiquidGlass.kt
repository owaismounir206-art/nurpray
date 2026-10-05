package com.nurpray.app.core.designsystem

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Liquid Glass Design System
 * Hyper-optimized 120Hz fluid glassmorphism engine with dynamic refraction,
 * liquid gradient orbs, specular border highlights, and spring micro-interactions.
 */

@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidMeshAnimation")

    // Fluid phase animations running at 120fps with zero layout recomposition
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val baseBgColor = if (isDark) Color(0xFF090D0B) else Color(0xFFF2F6F4)
    val orb1Color = if (isDark) Color(0xFF0F5132).copy(alpha = 0.55f) else Color(0xFF8FD8B3).copy(alpha = 0.6f)
    val orb2Color = if (isDark) Color(0xFF143642).copy(alpha = 0.65f) else Color(0xFFA2D2FF).copy(alpha = 0.5f)
    val orb3Color = if (isDark) Color(0xFF5A3E1B).copy(alpha = 0.45f) else Color(0xFFFFE5B4).copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBgColor)
    ) {
        // GPU-rendered organic liquid mesh canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Orb 1: Emerald Life Blob
            val orb1X = width * (0.35f + 0.25f * cos(phase1))
            val orb1Y = height * (0.25f + 0.20f * sin(phase1))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb1Color, orb1Color.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(orb1X, orb1Y),
                    radius = width * 0.75f
                )
            )

            // Orb 2: Deep Cyan Oceanic Blob
            val orb2X = width * (0.70f + 0.20f * sin(phase2))
            val orb2Y = height * (0.65f + 0.25f * cos(phase2))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb2Color, orb2Color.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(orb2X, orb2Y),
                    radius = width * 0.85f
                )
            )

            // Orb 3: Warm Amber Sun Blob
            val orb3X = width * (0.45f - 0.20f * cos(phase1 * 0.7f))
            val orb3Y = height * (0.85f + 0.15f * sin(phase2 * 0.8f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb3Color, orb3Color.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(orb3X, orb3Y),
                    radius = width * 0.65f
                )
            )
        }

        // Subtly layered translucent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) Color.Black.copy(alpha = 0.25f)
                    else Color.White.copy(alpha = 0.35f)
                )
        )

        content()
    }
}

/**
 * Reusable Liquid Glass Card with Specular Gradient Border & Spring Rebound Physics
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    isHighlighted: Boolean = false,
    highlightColor: Color = AmberGold,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    // Ultra-fluid Spring scale bounce on touch
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glassScale"
    )

    // Glass Background Gradient
    val glassFillBrush = if (isDark) {
        if (isHighlighted) {
            Brush.verticalGradient(
                colors = listOf(
                    highlightColor.copy(alpha = 0.22f),
                    highlightColor.copy(alpha = 0.08f),
                    Color(0xFF1E2824).copy(alpha = 0.75f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.11f),
                    Color.White.copy(alpha = 0.04f),
                    Color(0xFF161C19).copy(alpha = 0.70f)
                )
            )
        }
    } else {
        if (isHighlighted) {
            Brush.verticalGradient(
                colors = listOf(
                    highlightColor.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.85f),
                    Color.White.copy(alpha = 0.70f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.88f),
                    Color.White.copy(alpha = 0.65f)
                )
            )
        }
    }

    // Specular border reflection (white-to-transparent rim light)
    val specularBorder = if (isHighlighted) {
        BorderStroke(
            width = 1.3.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    highlightColor.copy(alpha = 0.85f),
                    Color.White.copy(alpha = 0.5f),
                    highlightColor.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                start = Offset(0f, 0f),
                end = Offset(400f, 400f)
            )
        )
    } else {
        BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.35f else 0.8f),
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent,
                    Color.White.copy(alpha = if (isDark) 0.15f else 0.4f)
                ),
                start = Offset(0f, 0f),
                end = Offset(300f, 300f)
            )
        )
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        }
                    )
                } else Modifier
            ),
        shape = shape,
        color = Color.Transparent,
        border = specularBorder,
        shadowElevation = if (isHighlighted) 12.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(glassFillBrush)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Liquid Glass Quick Action Button with Specular Rim & Spring Physics
 */
@Composable
fun LiquidGlassButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = EmeraldLight
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnScale"
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.35f else 0.7f),
                    Color.Transparent,
                    accentColor.copy(alpha = 0.3f)
                )
            )
        ),
        modifier = modifier
            .height(76.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) Color.White.copy(alpha = 0.07f)
                    else Color.White.copy(alpha = 0.72f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = if (isDark) Color.White else Color(0xFF1E2824),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Liquid Progress Arc with Continuous Glowing Bead Indicator
 */
@Composable
fun LiquidProgressArc(
    progress: Float,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 220.dp,
    trackColor: Color = Color.White.copy(alpha = 0.15f),
    liquidColors: List<Color> = listOf(AmberGold, Color(0xFFFFD56B), EmeraldLight)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquidArcProgress"
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val strokeWidth = 12.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

        // Background track with soft glow
        drawArc(
            color = trackColor,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Shimmering Liquid Arc
        val brush = Brush.sweepGradient(
            colors = liquidColors,
            center = Offset(size.width / 2, size.height / 2)
        )

        val sweep = 270f * animatedProgress
        if (sweep > 0f) {
            drawArc(
                brush = brush,
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Pulsing bead on the tip
            val tipAngleRad = Math.toRadians((135f + sweep).toDouble())
            val radius = diameter / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            val tipX = centerX + radius * cos(tipAngleRad).toFloat()
            val tipY = centerY + radius * sin(tipAngleRad).toFloat()

            // Outer bead glow
            drawCircle(
                color = AmberGold.copy(alpha = 0.45f),
                radius = strokeWidth * 0.9f,
                center = Offset(tipX, tipY)
            )
            // Solid center bead
            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.4f,
                center = Offset(tipX, tipY)
            )
        }
    }
}
