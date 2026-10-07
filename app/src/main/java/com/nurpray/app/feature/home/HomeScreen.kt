package com.nurpray.app.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurpray.app.R
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
    onNavigateToDua: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val schedule = uiState.schedule
    val haptic = LocalHapticFeedback.current

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
                            text = stringResource(R.string.app_name),
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
                                contentDescription = stringResource(R.string.change_city),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        M3Background {
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
                        M3Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
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
                                        color = AmberGold.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "⭐ $event",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AmberGold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Fasting Tracker Card (Imsak & Iftar)
                item {
                    schedule?.let { sched ->
                        FastingTrackerCard(schedule = sched)
                    }
                }

                // Hero Prayer Card with Countdown & Progress Arc
                item {
                    schedule?.let { sched ->
                        HeroCountdownCard(
                            schedule = sched,
                            countdown = uiState.formattedCountdown
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
                        },
                        onDuaClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToDua()
                        }
                    )
                }

                // Section Header
                item {
                    Text(
                        text = stringResource(R.string.today_prayer_times),
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
    modifier: Modifier = Modifier
) {
    M3Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        isHighlighted = true,
        highlightColor = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            contentAlignment = Alignment.Center
        ) {
            M3ProgressArc(
                progress = schedule.progressRatio,
                sizeDp = 190.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                progressColor = MaterialTheme.colorScheme.primary
            )

            // Central Countdown Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${stringResource(R.string.next_label)} • ${stringResource(schedule.nextPrayer.type.nameResId)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = countdown,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.starts_at, schedule.nextPrayer.formattedTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FastingTrackerCard(
    schedule: com.nurpray.app.domain.model.TodayPrayerSchedule,
    modifier: Modifier = Modifier
) {
    val fajr = schedule.prayers.firstOrNull { it.type == PrayerType.FAJR }?.time
    val maghrib = schedule.prayers.firstOrNull { it.type == PrayerType.MAGHRIB }?.time

    if (fajr != null && maghrib != null) {
        val imsakTime = fajr.minusMinutes(10)
        val now = java.time.LocalTime.now()

        val isFastingNow = now.isAfter(fajr) && now.isBefore(maghrib)

        val countdownLabel = if (isFastingNow) {
            val mins = java.time.Duration.between(now, maghrib).toMinutes()
            val h = mins / 60
            val m = mins % 60
            stringResource(R.string.iftar_in, "${h}h ${m}m")
        } else if (now.isBefore(imsakTime)) {
            val mins = java.time.Duration.between(now, imsakTime).toMinutes()
            val h = mins / 60
            val m = mins % 60
            stringResource(R.string.imsak_in, "${h}h ${m}m")
        } else {
            stringResource(R.string.fasting_completed_today)
        }

        M3Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            isHighlighted = isFastingNow,
            highlightColor = AmberGold
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌙",
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.fasting_and_iftar),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isFastingNow) AmberGold else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = countdownLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${stringResource(R.string.imsak)}: ${imsakTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stringResource(R.string.iftar)}: ${maghrib.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onQiblaClick: () -> Unit,
    onTasbihClick: () -> Unit,
    onQuranClick: () -> Unit,
    onDuaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        M3QuickActionButton(
            title = stringResource(R.string.nav_qibla),
            icon = Icons.Default.Explore,
            onClick = onQiblaClick,
            modifier = Modifier.weight(1f),
            accentColor = EmeraldLight
        )
        M3QuickActionButton(
            title = stringResource(R.string.nav_tasbih),
            icon = Icons.Default.Fingerprint,
            onClick = onTasbihClick,
            modifier = Modifier.weight(1f),
            accentColor = AmberGold
        )
        M3QuickActionButton(
            title = stringResource(R.string.nav_quran),
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = onQuranClick,
            modifier = Modifier.weight(1f),
            accentColor = Color(0xFF64B5F6)
        )
        M3QuickActionButton(
            title = stringResource(R.string.nav_dua),
            icon = Icons.Default.Favorite,
            onClick = onDuaClick,
            modifier = Modifier.weight(1f),
            accentColor = Color(0xFFE91E63)
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
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    M3Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        isHighlighted = isCurrent || isNext,
        highlightColor = highlightColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status Indicator Orb
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> EmeraldLight
                                isNext -> AmberGold
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            }
                        )
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(prayer.type.nameResId),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldLight.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "NOW",
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
