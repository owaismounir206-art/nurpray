package com.nurpray.app.feature.dua

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.nurpray.app.data.repository.DuaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Tutte") }
    var searchQuery by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    // Local state for counts
    val counts = remember { mutableStateMapOf<String, Int>() }

    val filteredDuas = remember(selectedCategory, searchQuery) {
        DuaRepository.duas.filter { dua ->
            (selectedCategory == "Tutte" || selectedCategory.isBlank() || dua.category == selectedCategory) &&
                    (searchQuery.isBlank() ||
                            dua.title.contains(searchQuery, ignoreCase = true) ||
                            dua.transliteration.contains(searchQuery, ignoreCase = true) ||
                            dua.italianTranslation.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dua_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_dua)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    items(DuaRepository.categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Duas List
            items(filteredDuas, key = { it.id }) { dua ->
                val currentCount = counts.getOrDefault(dua.id, 0)
                val isCompleted = currentCount >= dua.targetCount

                M3Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    isHighlighted = isCompleted,
                    highlightColor = EmeraldLight,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Title and Category Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dua.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberGold.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = dua.source,
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Arabic Calligraphy
                    Text(
                        text = dua.arabicText,
                        fontSize = 22.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Transliteration
                    Text(
                        text = dua.transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = EmeraldLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Translation
                    Text(
                        text = "« ${dua.italianTranslation} »",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive Counter Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.repetitions, dua.targetCount),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCompleted) EmeraldLight else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            modifier = Modifier.clickable {
                                if (isCompleted) {
                                    counts[dua.id] = 0
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else {
                                    val next = currentCount + 1
                                    counts[dua.id] = next
                                    if (next >= dua.targetCount) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.target_reached),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "$currentCount / ${dua.targetCount}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
