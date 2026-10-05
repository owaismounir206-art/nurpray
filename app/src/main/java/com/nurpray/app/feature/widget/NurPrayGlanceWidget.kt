package com.nurpray.app.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.nurpray.app.data.astronomical.CalculationParameters
import com.nurpray.app.domain.usecase.GetTodayPrayerTimesUseCase
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class NurPrayGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Compute today's prayer times for widget
        val useCase = GetTodayPrayerTimesUseCase()
        val zoneId = ZoneId.systemDefault()
        // Default Rome coordinates
        val schedule = useCase(
            latitude = 41.9028,
            longitude = 12.4964,
            zoneId = zoneId,
            date = LocalDate.now(zoneId),
            now = LocalTime.now(zoneId),
            params = CalculationParameters()
        )

        provideContent {
            GlanceTheme {
                WidgetContent(
                    nextPrayerName = schedule.nextPrayer.type.displayName,
                    nextPrayerTime = schedule.nextPrayer.formattedTime,
                    prayers = schedule.prayers.map { it.type.displayName to it.formattedTime }
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        nextPrayerName: String,
        nextPrayerTime: String,
        prayers: List<Pair<String, String>>
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Prossima Preghiera",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
            Text(
                text = nextPrayerName,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = nextPrayerTime,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Horizontal row with daily times
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for ((name, time) in prayers.take(5)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = GlanceModifier.padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = name.take(3),
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                        Text(
                            text = time,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
