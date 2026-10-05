package com.nurpray.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nurpray.app.data.astronomical.AsrJuristicMethod
import com.nurpray.app.data.astronomical.HighLatitudeRule
import com.nurpray.app.data.astronomical.PrayerMethod

import com.nurpray.app.core.designsystem.LiquidBackground
import com.nurpray.app.core.designsystem.LiquidGlassCard
import com.nurpray.app.core.designsystem.AmberGold
import com.nurpray.app.core.designsystem.EmeraldLight
import androidx.compose.ui.graphics.Color

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

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Impostazioni",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Astronomical Calculation Section
                item {
                    Text(
                        text = "Calcolo Astronomico",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    LiquidGlassCard(
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        // Method Selector
                        SettingItem(
                            title = "Convenzione di Calcolo",
                            subtitle = uiState.selectedMethod.title,
                            onClick = { showMethodDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Asr Juristic Selector
                        SettingItem(
                            title = "Metodo Giuridico Asr",
                            subtitle = if (uiState.selectedAsrMethod == AsrJuristicMethod.HANAFI) "Hanafi (ombra 2:1)" else "Shafi'i / Maliki / Hanbali (ombra 1:1)",
                            onClick = { showAsrDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // High Latitude Rule
                        SettingItem(
                            title = "Latitudini Elevate (Nordiche)",
                            subtitle = when (uiState.selectedHighLatitudeRule) {
                                HighLatitudeRule.ANGLE_BASED -> "Angle-Based (Consigliato)"
                                HighLatitudeRule.MIDDLE_OF_NIGHT -> "Metà della notte"
                                HighLatitudeRule.ONE_SEVENTH -> "Un settimo della notte"
                                HighLatitudeRule.NONE -> "Nessuna correzione"
                            },
                            onClick = { showHighLatDialog = true }
                        )
                    }
                }

                // Notifications & Pre-Adhan Section
                item {
                    Text(
                        text = "Allarmi e Notifiche",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    LiquidGlassCard(
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pre-Allarme Adhan",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${uiState.preAdhanMinutes} minuti prima dell'inizio",
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
                                        text = "Silenzioso durante la preghiera",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Attiva Non Disturbare per 20 minuti dall'inizio della preghiera",
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

                    LiquidGlassCard(
                        shape = RoundedCornerShape(26.dp),
                        isHighlighted = updateState is UpdateUiState.Available,
                        highlightColor = AmberGold
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Aggiornamenti Automatici",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Versione corrente: ${com.nurpray.app.BuildConfig.VERSION_NAME}",
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
                                            text = "Controlla",
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
                                                text = "🎉 Nuova versione v${state.version} disponibile!",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AmberGold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Dimensione: ${String.format("%.1f", state.sizeMb)} MB",
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
                                                    text = "Scarica e Installa Ora",
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
                                            text = "Download aggiornamento: ${(state.progress * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { state.progress },
                                            modifier = Modifier.fillMaxWidth().height(8.dp),
                                            color = EmeraldLight,
                                            trackColor = Color.White.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                                is UpdateUiState.UpToDate -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "✨ NurPray è aggiornato all'ultima versione disponibile!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                is UpdateUiState.Error -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "⚠️ ${state.message}",
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
                    LiquidGlassCard(
                        shape = RoundedCornerShape(26.dp),
                        isHighlighted = false
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Privacy & Open Source",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NurPray è 100% offline-first, gratuito e privo di qualsiasi tracker o pubblicità. Tutti i calcoli solari e della Qibla avvengono localmente sul dispositivo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Versione ${com.nurpray.app.BuildConfig.VERSION_NAME} • Design Liquid Glass con animazioni a 120Hz",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Method Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text(
                    text = "Convenzione di Calcolo",
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
                TextButton(onClick = { showMethodDialog = false }) { Text("Chiudi") }
            }
        )
    }

    // Asr Dialog
    if (showAsrDialog) {
        AlertDialog(
            onDismissRequest = { showAsrDialog = false },
            title = { Text("Metodo Giuridico Asr") },
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
                                if (method == AsrJuristicMethod.HANAFI) "Hanafi (Ombra 2:1)" else "Shafi'i / Maliki / Hanbali (Ombra 1:1)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAsrDialog = false }) { Text("Chiudi") }
            }
        )
    }

    // High Latitude Dialog
    if (showHighLatDialog) {
        AlertDialog(
            onDismissRequest = { showHighLatDialog = false },
            title = { Text("Regola Latitudini Elevate") },
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
                            Text(rule.name.replace("_", " "), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHighLatDialog = false }) { Text("Chiudi") }
            }
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
