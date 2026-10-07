package com.nurpray.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nurpray.app.R
import com.nurpray.app.core.designsystem.AmberGold
import com.nurpray.app.core.designsystem.EmeraldLight
import com.nurpray.app.core.designsystem.M3Background
import com.nurpray.app.core.designsystem.M3Card
import com.nurpray.app.data.astronomical.AsrJuristicMethod
import com.nurpray.app.data.astronomical.HighLatitudeRule
import com.nurpray.app.data.astronomical.PrayerMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showMethodDialog by remember { mutableStateOf(false) }
    var showAsrDialog by remember { mutableStateOf(false) }
    var showHighLatDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Language Selection Section
                item {
                    Text(
                        text = stringResource(R.string.language_section),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    M3Card(shape = RoundedCornerShape(20.dp)) {
                        val currentLangCode by viewModel.currentLanguage.collectAsState()
                        val currentLang = APP_SUPPORTED_LANGUAGES.find { it.code == currentLangCode }
                            ?: APP_SUPPORTED_LANGUAGES.first()

                        SettingItem(
                            title = stringResource(R.string.app_language),
                            subtitle = "${currentLang.displayName} (${currentLang.nativeName})",
                            onClick = { showLanguageDialog = true }
                        )
                    }
                }

                // Astronomical Calculation Section
                item {
                    Text(
                        text = stringResource(R.string.calculation_section),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    M3Card(shape = RoundedCornerShape(20.dp)) {
                        // Method Selector
                        SettingItem(
                            title = stringResource(R.string.calc_method),
                            subtitle = uiState.selectedMethod.title,
                            onClick = { showMethodDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Asr Juristic Selector
                        SettingItem(
                            title = stringResource(R.string.asr_method),
                            subtitle = if (uiState.selectedAsrMethod == AsrJuristicMethod.HANAFI)
                                stringResource(R.string.asr_hanafi)
                            else
                                stringResource(R.string.asr_standard),
                            onClick = { showAsrDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // High Latitude Rule
                        SettingItem(
                            title = stringResource(R.string.high_latitude_rule),
                            subtitle = when (uiState.selectedHighLatitudeRule) {
                                HighLatitudeRule.ANGLE_BASED -> stringResource(R.string.high_lat_angle)
                                HighLatitudeRule.MIDDLE_OF_NIGHT -> stringResource(R.string.high_lat_midnight)
                                HighLatitudeRule.ONE_SEVENTH -> stringResource(R.string.high_lat_seventh)
                                HighLatitudeRule.NONE -> stringResource(R.string.high_lat_none)
                            },
                            onClick = { showHighLatDialog = true }
                        )
                    }
                }

                // Notifications & Pre-Adhan Section
                item {
                    Text(
                        text = stringResource(R.string.notifications_section),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    M3Card(shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.pre_adhan_notifications),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${uiState.preAdhanMinutes} min",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Slider(
                                value = uiState.preAdhanMinutes.toFloat(),
                                onValueChange = { viewModel.updatePreAdhanMinutes(it.toInt()) },
                                valueRange = 0f..30f,
                                steps = 5
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Auto DND Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.dnd_mode),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = stringResource(R.string.dnd_mode_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.isAutoDndEnabled,
                                    onCheckedChange = { viewModel.toggleAutoDnd(it) }
                                )
                            }
                        }
                    }
                }

                // In-App Auto-Updater Section
                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val updateState by viewModel.updateState.collectAsState()

                    M3Card(
                        shape = RoundedCornerShape(20.dp),
                        isHighlighted = updateState is UpdateUiState.Available,
                        highlightColor = AmberGold
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.updates_section),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.app_version, com.nurpray.app.BuildConfig.VERSION_NAME),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (updateState is UpdateUiState.Checking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Button(
                                        onClick = { viewModel.checkForUpdates() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.check_updates),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            when (val state = updateState) {
                                is UpdateUiState.Available -> {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AmberGold.copy(alpha = 0.2f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = stringResource(R.string.update_available, state.version),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AmberGold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Size: ${String.format("%.1f", state.sizeMb)} MB",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { viewModel.downloadAndInstall(context, state.url) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.download_install),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                                is UpdateUiState.Downloading -> {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = stringResource(R.string.downloading_progress, (state.progress * 100).toInt()),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { state.progress },
                                            modifier = Modifier.fillMaxWidth().height(8.dp),
                                            color = EmeraldLight
                                        )
                                    }
                                }
                                is UpdateUiState.UpToDate -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.app_up_to_date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                is UpdateUiState.Error -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                else -> Unit
                            }
                        }
                    }
                }

                // About & Zero Trackers Banner
                item {
                    M3Card(shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = stringResource(R.string.about_section),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.about_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.app_version, com.nurpray.app.BuildConfig.VERSION_NAME),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        val currentLangCode by viewModel.currentLanguage.collectAsState()
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.app_language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(APP_SUPPORTED_LANGUAGES.size) { index ->
                        val lang = APP_SUPPORTED_LANGUAGES[index]
                        val isSelected = lang.code == currentLangCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(lang.code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setLanguage(lang.code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < APP_SUPPORTED_LANGUAGES.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("OK") }
            }
        )
    }

    // Method Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.calc_method),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(PrayerMethod.entries.size) { index ->
                        val method = PrayerMethod.entries[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateMethod(method)
                                    showMethodDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = method == uiState.selectedMethod,
                                onClick = {
                                    viewModel.updateMethod(method)
                                    showMethodDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = method.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (method == uiState.selectedMethod) FontWeight.Bold else FontWeight.Normal,
                                    color = if (method == uiState.selectedMethod) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (index < PrayerMethod.entries.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) { Text("OK") }
            }
        )
    }

    // Asr Dialog
    if (showAsrDialog) {
        AlertDialog(
            onDismissRequest = { showAsrDialog = false },
            title = { Text(stringResource(R.string.asr_method)) },
            text = {
                Column {
                    AsrJuristicMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateAsrMethod(method)
                                    showAsrDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = method == uiState.selectedAsrMethod,
                                onClick = {
                                    viewModel.updateAsrMethod(method)
                                    showAsrDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (method == AsrJuristicMethod.HANAFI)
                                    stringResource(R.string.asr_hanafi)
                                else
                                    stringResource(R.string.asr_standard),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAsrDialog = false }) { Text("OK") }
            }
        )
    }

    // High Latitude Dialog
    if (showHighLatDialog) {
        AlertDialog(
            onDismissRequest = { showHighLatDialog = false },
            title = { Text(stringResource(R.string.high_latitude_rule)) },
            text = {
                Column {
                    HighLatitudeRule.values().forEach { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateHighLatitudeRule(rule)
                                    showHighLatDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = rule == uiState.selectedHighLatitudeRule,
                                onClick = {
                                    viewModel.updateHighLatitudeRule(rule)
                                    showHighLatDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (rule) {
                                    HighLatitudeRule.ANGLE_BASED -> stringResource(R.string.high_lat_angle)
                                    HighLatitudeRule.MIDDLE_OF_NIGHT -> stringResource(R.string.high_lat_midnight)
                                    HighLatitudeRule.ONE_SEVENTH -> stringResource(R.string.high_lat_seventh)
                                    HighLatitudeRule.NONE -> stringResource(R.string.high_lat_none)
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHighLatDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
