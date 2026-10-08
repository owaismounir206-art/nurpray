package com.nurpray.app.feature.qibla

import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurpray.app.R
import com.nurpray.app.core.designsystem.AmberGold
import com.nurpray.app.core.designsystem.EmeraldLight
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.qibla_compass),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.cityName.isNotBlank()) {
                            Text(
                                text = uiState.cityName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Low accuracy warning
            if (uiState.sensorAccuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            text = stringResource(R.string.sensor_low_accuracy),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Compass Dial (responsive 260dp)
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Dial Outer Ring
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.25f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Tick Marks
                    rotate(animatedRotation, pivot = center) {
                        for (i in 0 until 360 step 15) {
                            val angleRad = Math.toRadians(i.toDouble())
                            val isMajor = i % 90 == 0
                            val tickLen = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                            val strokeW = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                            val tickColor = if (isMajor) Color.Gray else Color.Gray.copy(alpha = 0.4f)

                            val startX = center.x + (radius - tickLen) * sin(angleRad).toFloat()
                            val startY = center.y - (radius - tickLen) * cos(angleRad).toFloat()
                            val endX = center.x + radius * sin(angleRad).toFloat()
                            val endY = center.y - radius * cos(angleRad).toFloat()

                            drawLine(
                                color = tickColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = strokeW
                            )
                        }

                        // Cardinal North Pointer
                        val northPath = Path().apply {
                            moveTo(center.x, center.y - radius + 20.dp.toPx())
                            lineTo(center.x - 7.dp.toPx(), center.y - radius + 38.dp.toPx())
                            lineTo(center.x + 7.dp.toPx(), center.y - radius + 38.dp.toPx())
                            close()
                        }
                        drawPath(path = northPath, color = Color(0xFFE53935))
                    }

                    // Qibla Indicator Needle
                    rotate(animatedQiblaAngle, pivot = center) {
                        val needlePath = Path().apply {
                            moveTo(center.x, center.y - radius + 15.dp.toPx())
                            lineTo(center.x - 10.dp.toPx(), center.y)
                            lineTo(center.x + 10.dp.toPx(), center.y)
                            close()
                        }
                        drawPath(path = needlePath, color = activeColor)

                        val tailPath = Path().apply {
                            moveTo(center.x, center.y + radius - 25.dp.toPx())
                            lineTo(center.x - 10.dp.toPx(), center.y)
                            lineTo(center.x + 10.dp.toPx(), center.y)
                            close()
                        }
                        drawPath(path = tailPath, color = Color.Gray.copy(alpha = 0.3f))
                    }

                    // Center Kaaba Emblem Ring
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

            // Info Card (Azimuth, Alignment, Distance)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.kaaba_direction),
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
                            text = stringResource(R.string.distance),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.distance_km, uiState.bearing.distanceToKaabaKm.roundToInt()),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Alignment Banner
                Surface(
                    color = if (uiState.bearing.isAligned) EmeraldLight.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
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
                            text = if (uiState.bearing.isAligned)
                                stringResource(R.string.qibla_aligned)
                            else
                                stringResource(R.string.rotate_device),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (uiState.bearing.isAligned) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.bearing.isAligned) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
