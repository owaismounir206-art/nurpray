package com.nurpray.app.feature.qibla

import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurpray.app.core.designsystem.AmberGold
import com.nurpray.app.core.designsystem.EmeraldDeep
import com.nurpray.app.core.designsystem.EmeraldLight
import kotlin.math.*

import com.nurpray.app.core.designsystem.LiquidBackground
import com.nurpray.app.core.designsystem.LiquidGlassCard

@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Trigger haptic bump when entering alignment
    LaunchedEffect(uiState.bearing.isAligned) {
        if (uiState.bearing.isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val animatedRotation by animateFloatAsState(
        targetValue = -uiState.bearing.deviceHeadingDegrees,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "compassRotation"
    )

    val targetQiblaRelative = uiState.bearing.relativeAngleDegrees
    val animatedQiblaAngle by animateFloatAsState(
        targetValue = targetQiblaRelative,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "qiblaPointerRotation"
    )

    val activeColor by animateColorAsState(
        targetValue = if (uiState.bearing.isAligned) EmeraldLight else AmberGold,
        animationSpec = tween(300),
        label = "alignedColor"
    )

    LiquidBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Bussola Qibla",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = uiState.cityName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        // Low accuracy warning
        if (uiState.sensorAccuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sensore bussola non calibrato. Muovi il dispositivo formando un 8 nell'aria.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Custom Compass Dial
        Box(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 16.dp.toPx()

                // Outer Dial (rotates with device heading to mimic a physical compass)
                rotate(animatedRotation, pivot = center) {
                    // Outer Ring
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Degree tick marks
                    for (degree in 0 until 360 step 15) {
                        val isMajor = degree % 45 == 0
                        val tickLength = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                        val tickWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                        val rad = Math.toRadians(degree.toDouble())

                        val outerX = center.x + radius * sin(rad).toFloat()
                        val outerY = center.y - radius * cos(rad).toFloat()
                        val innerX = center.x + (radius - tickLength) * sin(rad).toFloat()
                        val innerY = center.y - (radius - tickLength) * cos(rad).toFloat()

                        val tickColor = if (degree == 0) Color.Red else Color.Gray.copy(alpha = 0.6f)
                        drawLine(
                            color = tickColor,
                            start = Offset(innerX, innerY),
                            end = Offset(outerX, outerY),
                            strokeWidth = tickWidth
                        )
                    }
                }

                // Qibla Pointer Needle
                rotate(animatedQiblaAngle, pivot = center) {
                    // Emerald / Amber Arrow pointing directly to Mecca
                    val needlePath = Path().apply {
                        moveTo(center.x, center.y - radius + 10.dp.toPx()) // Tip
                        lineTo(center.x - 14.dp.toPx(), center.y)
                        lineTo(center.x, center.y - 8.dp.toPx())
                        lineTo(center.x + 14.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = needlePath,
                        brush = Brush.verticalGradient(
                            listOf(activeColor, activeColor.copy(alpha = 0.6f))
                        )
                    )

                    // Tail Arrow (opposite side)
                    val tailPath = Path().apply {
                        moveTo(center.x, center.y + radius - 30.dp.toPx())
                        lineTo(center.x - 10.dp.toPx(), center.y)
                        lineTo(center.x + 10.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = tailPath,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                }

                // Center Kaaba Emblem
                drawCircle(
                    color = activeColor,
                    radius = 24.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.Black,
                    radius = 18.dp.toPx(),
                    center = center
                )
            }

            // Central Kaaba Cube Representation
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black)
            )
        }

        // Info Cards (Azimuth, Alignment, Distance) - Liquid Glass
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            isHighlighted = uiState.bearing.isAligned,
            highlightColor = EmeraldLight
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Direzione Kaaba",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.bearing.qiblaDirectionDegrees.roundToInt()}°",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Distanza",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.bearing.distanceToKaabaKm.roundToInt()} km",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alignment Banner
            Surface(
                color = if (uiState.bearing.isAligned) EmeraldLight.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (uiState.bearing.isAligned) Icons.Default.CheckCircle else Icons.Default.Explore,
                        contentDescription = null,
                        tint = if (uiState.bearing.isAligned) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.bearing.isAligned) "Perfettamente allineato alla Qibla! 🕋" else "Ruota il dispositivo verso l'indicatore dorato",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (uiState.bearing.isAligned) FontWeight.Bold else FontWeight.Normal,
                        color = if (uiState.bearing.isAligned) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
}
