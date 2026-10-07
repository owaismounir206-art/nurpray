package com.nurpray.app.feature.tasbih

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.nurpray.app.core.designsystem.M3Card

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeDhikr = uiState.activeDhikr
    val haptic = LocalHapticFeedback.current

    val progress = if (activeDhikr.target > 0) {
        (activeDhikr.count.toFloat() / activeDhikr.target.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tasbihProgress"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.digital_tasbih),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.resetActiveCount() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.reset_counter)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dhikr Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(uiState.dhikrList) { index, item ->
                    val isSelected = index == uiState.currentDhikrIndex
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectDhikr(index) },
                        label = { Text(item.transliteration) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Dhikr Calligraphy & Meaning Card
            M3Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeDhikr.arabicText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeDhikr.transliteration,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activeDhikr.translation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Large Material Counter Card
            val primaryColor = MaterialTheme.colorScheme.primary
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            M3Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(28.dp),
                isHighlighted = true,
                highlightColor = EmeraldLight,
                contentPadding = PaddingValues(16.dp),
                onClick = {
                    val reached = viewModel.incrementCount()
                    if (reached) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Circular Track & Progress Ring
                    Canvas(modifier = Modifier.size(210.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        // Track
                        drawCircle(
                            color = surfaceVariant.copy(alpha = 0.5f),
                            radius = size.minDimension / 2 - strokeWidth / 2,
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress Arc
                        if (animatedProgress > 0f) {
                            drawArc(
                                color = primaryColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Count Text Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${activeDhikr.count}",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/ ${activeDhikr.target}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (activeDhikr.count >= activeDhikr.target) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldLight.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = stringResource(R.string.target_reached),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.tap_to_count),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
