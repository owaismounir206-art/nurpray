package com.nurpray.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.nurpray.app.core.designsystem.NurPrayTheme
import com.nurpray.app.feature.home.HomeScreen
import com.nurpray.app.feature.home.HomeViewModel
import com.nurpray.app.feature.qibla.QiblaScreen
import com.nurpray.app.feature.qibla.QiblaViewModel
import com.nurpray.app.feature.quran.QuranScreen
import com.nurpray.app.feature.quran.QuranViewModel
import com.nurpray.app.feature.settings.SettingsScreen
import com.nurpray.app.feature.settings.SettingsViewModel
import com.nurpray.app.feature.tasbih.TasbihScreen
import com.nurpray.app.feature.tasbih.TasbihViewModel

import com.nurpray.app.feature.dua.DuaScreen
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import com.nurpray.app.R

enum class AppDestination(val labelResId: Int, val icon: ImageVector) {
    HOME(R.string.nav_home, Icons.Default.Home),
    QIBLA(R.string.nav_qibla, Icons.Default.Explore),
    QURAN(R.string.nav_quran, Icons.AutoMirrored.Filled.MenuBook),
    TASBIH(R.string.nav_tasbih, Icons.Default.Fingerprint),
    DUA(R.string.nav_dua, Icons.Default.Favorite),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings)
}

class MainActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val qiblaViewModel: QiblaViewModel by viewModels()
    private val tasbihViewModel: TasbihViewModel by viewModels()
    private val quranViewModel: QuranViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            homeViewModel.requestGpsLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestRequiredPermissions()

        // Sync location with Qibla compass
        lifecycleScope.launch {
            homeViewModel.uiState.collect { state ->
                val loc = state.location
                qiblaViewModel.setLocation(loc.latitude, loc.longitude, "${loc.cityName}, ${loc.countryName}")
            }
        }

        // Sync calculation settings with HomeViewModel
        lifecycleScope.launch {
            settingsViewModel.uiState.collect { settings ->
                homeViewModel.updateCalculationParameters(settings.toCalculationParameters())
            }
        }

        setContent {
            NurPrayTheme {
                var currentDestination by remember { mutableStateOf(AppDestination.HOME) }

                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        AppDestination.values().forEach { destination ->
                            item(
                                icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelResId)) },
                                label = { Text(stringResource(destination.labelResId)) },
                                selected = destination == currentDestination,
                                onClick = { currentDestination = destination }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (currentDestination) {
                        AppDestination.HOME -> HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToQibla = { currentDestination = AppDestination.QIBLA },
                            onNavigateToTasbih = { currentDestination = AppDestination.TASBIH },
                            onNavigateToQuran = { currentDestination = AppDestination.QURAN },
                            onNavigateToDua = { currentDestination = AppDestination.DUA },
                            onNavigateToSettings = { currentDestination = AppDestination.SETTINGS },
                            modifier = Modifier.fillMaxSize()
                        )
                        AppDestination.QIBLA -> QiblaScreen(
                            viewModel = qiblaViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        AppDestination.QURAN -> QuranScreen(
                            viewModel = quranViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        AppDestination.TASBIH -> TasbihScreen(
                            viewModel = tasbihViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        AppDestination.DUA -> DuaScreen(
                            onBack = { currentDestination = AppDestination.HOME },
                            modifier = Modifier.fillMaxSize()
                        )
                        AppDestination.SETTINGS -> SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { currentDestination = AppDestination.HOME },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}
