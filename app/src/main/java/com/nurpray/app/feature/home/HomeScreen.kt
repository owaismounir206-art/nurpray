package com.nurpray.app.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurpray.app.core.designsystem.*
import com.nurpray.app.domain.model.PrayerTime
import com.nurpray.app.domain.model.PrayerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToQibla: () -> Unit,
    onNavigateToTasbih: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val schedule = uiState.schedule
    val haptic = LocalHapticFeedback.current

    // Determine ambient gradient based on active prayer
    val activeGradient = when (uiState.activePrayerType) {
        PrayerType.FAJR -> FajrSkyGradient
        PrayerType.SUNRISE -> SunriseSkyGradient
        PrayerType.DHUHR -> DhuhrSkyGradient
        PrayerType.ASR -> AsrSkyGradient
        PrayerType.MAGHRIB -> MaghribSkyGradient
        PrayerType.ISHA -> IshaSkyGradient
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NurPray",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.location.cityName}, ${uiState.location.countryName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Prayer Card with Arc Progress and Countdown
            item {
                schedule?.let { sched ->
                    HeroCountdownCard(
                        schedule = sched,
                        countdown = uiState.formattedCountdown,
                        gradient = activeGradient
                    )
                }
            }

            // Quick Actions Bar
            item {
                QuickActionsRow(
                    onQiblaClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToQibla()
                    },
                    onTasbihClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToTasbih()
                    },
                    onQuranClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToQuran()
                    }
                )
            }

            // Section Header
            item {
                Text(
                    text = "Orari delle Preghiere di Oggi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Today's 6 Prayer Cards
            schedule?.prayers?.let { prayers ->
                items(prayers) { prayer ->
                    val isCurrent = prayer.type == schedule.currentPrayer?.type
                    val isNext = prayer.type == schedule.nextPrayer.type
                    PrayerTimeCard(
                        prayer = prayer,
                        isCurrent = isCurrent,
                        isNext = isNext
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HeroCountdownCard(
    schedule: com.nurpray.app.domain.model.TodayPrayerSchedule,
    countdown: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = schedule.progressRatio,
        animationSpec = tween(durationMillis = 600),
        label = "arcProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background subtle circular arc
            Canvas(modifier = Modifier.size(200.dp)) {
                val strokeWidth = 10.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                val arcSize = Size(diameter, diameter)

                // Background track
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Animated Active Progress
                drawArc(
                    color = AmberGold,
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Central Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Prossima: ${schedule.nextPrayer.type.displayName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Orario: ${schedule.nextPrayer.formattedTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onQiblaClick: () -> Unit,
    onTasbihClick: () -> Unit,
    onQuranClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            title = "Qibla",
            icon = Icons.Default.Explore,
            onClick = onQiblaClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "Tasbih",
            icon = Icons.Default.Fingerprint,
            onClick = onTasbihClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "Corano",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = onQuranClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.height(72.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PrayerTimeCard(
    prayer: PrayerTime,
    isCurrent: Boolean,
    isNext: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        isNext -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val contentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
        isNext -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon or dot indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent || isNext) contentColor else Color.Transparent)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = prayer.type.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                    Text(
                        text = prayer.type.arabicName,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = prayer.formattedTime,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
