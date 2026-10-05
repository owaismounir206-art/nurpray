package com.nurpray.app.feature.tasbih

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurpray.app.core.designsystem.AmberGold
import com.nurpray.app.core.designsystem.EmeraldDeep
import com.nurpray.app.core.designsystem.EmeraldLight

import com.nurpray.app.core.designsystem.LiquidBackground
import com.nurpray.app.core.designsystem.LiquidGlassCard

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

    LiquidBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasbih Digitale",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { viewModel.resetActiveCount() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Azzera contatore",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // Dhikr Calligraphy & Translation Card - Liquid Glass
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeDhikr.arabicText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeDhikr.transliteration,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = activeDhikr.translation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Liquid Counter Glass Card
            LiquidGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(36.dp),
                isHighlighted = true,
                highlightColor = EmeraldLight,
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
                    // Shimmering Circular Ring
                    Canvas(modifier = Modifier.size(240.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        // Track
                        drawCircle(
                            color = Color.White.copy(alpha = 0.12f),
                            radius = size.minDimension / 2 - strokeWidth / 2,
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress Arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(EmeraldLight, AmberGold, Color(0xFF64B5F6), EmeraldLight)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Big Counter Display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${activeDhikr.count}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (activeDhikr.target > 0) {
                            Text(
                                text = "Obiettivo: ${activeDhikr.target}",
                                style = MaterialTheme.typography.titleMedium,
                                color = AmberGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tocca per contare",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
