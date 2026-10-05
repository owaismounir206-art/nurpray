package com.nurpray.app.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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

    var showCityPicker by remember { mutableStateOf(false) }
    val citiesList by viewModel.citiesList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isGpsLoading by viewModel.isGpsLoading.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NurPray",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCityPicker = true }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
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
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Cambia città",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
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
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LiquidBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hijri Date and Islamic Holy Events Banner
                item {
                    val hijriDate = remember(schedule?.date) {
                        schedule?.date?.let { com.nurpray.app.data.astronomical.HijriCalendarHelper.gregorianToHijri(it) }
                    }

                    hijriDate?.let { hDate ->
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = hDate.formattedLatin,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = hDate.formattedArabic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (hDate.specialEvents.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                for (event in hDate.specialEvents) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AmberGold.copy(alpha = 0.22f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "✨ ${event.title}: ${event.description}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Hero Prayer Card with Liquid Progress Arc and Countdown
                item {
                    schedule?.let { sched ->
                        HeroCountdownCard(
                            schedule = sched,
                            countdown = uiState.formattedCountdown,
                            gradient = activeGradient
                        )
                    }
                }

                // Quick Actions Bar (Liquid Glass Buttons)
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

        if (showCityPicker) {
            CityPickerSheet(
                onDismissRequest = { showCityPicker = false },
                onCitySelected = { loc ->
                    viewModel.updateLocation(loc)
                    showCityPicker = false
                },
                onGpsRequested = { viewModel.requestGpsLocation() },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                citiesList = citiesList,
                isGpsLoading = isGpsLoading
            )
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
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        isHighlighted = true,
        highlightColor = AmberGold
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            // Hyper-optimized Liquid Progress Arc with glowing bead
            LiquidProgressArc(
                progress = schedule.progressRatio,
                sizeDp = 200.dp,
                trackColor = Color.White.copy(alpha = 0.12f),
                liquidColors = listOf(AmberGold, Color(0xFFFFD56B), EmeraldLight)
            )

            // Central Glowing Countdown Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AmberGold.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Prossima • ${schedule.nextPrayer.type.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = countdown,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Inizio alle ${schedule.nextPrayer.formattedTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
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
        LiquidGlassButton(
            title = "Qibla",
            icon = Icons.Default.Explore,
            onClick = onQiblaClick,
            modifier = Modifier.weight(1f),
            accentColor = EmeraldLight
        )
        LiquidGlassButton(
            title = "Tasbih",
            icon = Icons.Default.Fingerprint,
            onClick = onTasbihClick,
            modifier = Modifier.weight(1f),
            accentColor = AmberGold
        )
        LiquidGlassButton(
            title = "Corano",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = onQuranClick,
            modifier = Modifier.weight(1f),
            accentColor = Color(0xFF64B5F6)
        )
    }
}

@Composable
fun PrayerTimeCard(
    prayer: PrayerTime,
    isCurrent: Boolean,
    isNext: Boolean,
    modifier: Modifier = Modifier
) {
    val highlightColor = when {
        isCurrent -> EmeraldLight
        isNext -> AmberGold
        else -> Color.Transparent
    }

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        isHighlighted = isCurrent || isNext,
        highlightColor = highlightColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulsing Liquid Status Orb
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> EmeraldLight
                                isNext -> AmberGold
                                else -> Color.White.copy(alpha = 0.25f)
                            }
                        )
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.type.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isCurrent || isNext) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldLight.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "ORA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = prayer.type.arabicName,
                        style = MaterialTheme.typography.bodySmall,
                        color = (if (isCurrent || isNext) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = prayer.formattedTime,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) EmeraldLight else if (isNext) AmberGold else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
