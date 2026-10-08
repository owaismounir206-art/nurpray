package com.nurpray.app.feature.home

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

    // Ambient gradient based on active prayer
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
        containerColor = MaterialTheme.colorScheme.background,
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hijri Date and Islamic Events Banner
            item {
                val hijriDate = remember(schedule?.date) {
                    schedule?.date?.let { com.nurpray.app.data.astronomical.HijriCalendarHelper.gregorianToHijri(it) }
                }

                hijriDate?.let { hDate ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
            }

            // Fasting Tracker Card (Suhoor / Iftar)
            item {
                schedule?.let { sched ->
                    FastingTrackerCard(schedule = sched)
                }
            }

            // Hero Prayer Card with Ambient Sky Gradient, Progress Arc & Countdown
            item {
                schedule?.let { sched ->
                    HeroCountdownCard(
                        schedule = sched,
                        countdown = uiState.formattedCountdown,
                        gradient = activeGradient
                    )
                }
            }

            // Quick Actions Bar (Qibla, Tasbih, Corano, Du'a)
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
    val animatedProgress by animateFloatAsState(
        targetValue = schedule.progressRatio,
        animationSpec = tween(durationMillis = 600),
        label = "arcProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp),
        shape = RoundedCornerShape(26.dp),
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
            Canvas(modifier = Modifier.size(190.dp)) {
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
                if (animatedProgress > 0f) {
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
            }

            // Central Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${stringResource(R.string.next_label)}: ${stringResource(schedule.nextPrayer.type.nameResId)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.starts_at, schedule.nextPrayer.formattedTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberGold,
                    fontWeight = FontWeight.SemiBold
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

        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
        QuickActionButton(
            title = stringResource(R.string.nav_qibla),
            icon = Icons.Default.Explore,
            onClick = onQiblaClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = stringResource(R.string.nav_tasbih),
            icon = Icons.Default.Fingerprint,
            onClick = onTasbihClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = stringResource(R.string.nav_quran),
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = onQuranClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = stringResource(R.string.nav_dua),
            icon = Icons.Default.Favorite,
            onClick = onDuaClick,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
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
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val contentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
        isNext -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicator Dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent || isNext) contentColor else contentColor.copy(alpha = 0.25f))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(prayer.type.nameResId),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.SemiBold,
                            color = contentColor
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = contentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = stringResource(R.string.current_prayer),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
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
