package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AppLanguage
import com.example.ui.model.DailyForecastItem
import com.example.ui.model.WeatherType
import com.example.ui.util.WeatherLocalization
import kotlin.math.roundToInt

@Composable
fun DailyForecastList(
    dailyList: List<DailyForecastItem>,
    selectedLanguage: AppLanguage = AppLanguage.PT,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    val minOverall = dailyList.minOfOrNull { it.minTemp } ?: 10.0
    val maxOverall = dailyList.maxOfOrNull { it.maxTemp } ?: 35.0
    val tempRange = (maxOverall - minOverall).coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(
                color = Color.Black.copy(alpha = 0.22f),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
            .testTag("daily_forecast_container")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = WeatherLocalization.getDailyHeader(selectedLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            )
        }

        dailyList.forEachIndexed { index, item ->
            DailyForecastRowItem(
                index = index,
                item = item,
                minOverall = minOverall,
                tempRange = tempRange,
                selectedLanguage = selectedLanguage
            )
            if (index < dailyList.lastIndex) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.08f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyForecastRowItem(
    index: Int,
    item: DailyForecastItem,
    minOverall: Double,
    tempRange: Double,
    selectedLanguage: AppLanguage
) {
    val condition = WeatherType.fromWmo(item.weatherCode, isDay = true, lang = selectedLanguage)
    val dayText = WeatherLocalization.getDayLabel(index, item.dateLabel, selectedLanguage)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day name
        Text(
            text = dayText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                color = Color.White
            ),
            modifier = Modifier.width(72.dp)
        )

        // Weather icon & rain chance
        Row(
            modifier = Modifier.width(54.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = condition.icon,
                contentDescription = condition.description,
                tint = Color(0xFFFFEB3B),
                modifier = Modifier.size(24.dp)
            )
            if (item.precipitationProbability > 15) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.precipitationProbability}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = Color(0xFF81D4FA),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Min temp
        Text(
            text = "${item.minTemp.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(32.dp)
        )

        // Temperature Range Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            val startRatio = ((item.minTemp - minOverall) / tempRange).toFloat().coerceIn(0f, 0.9f)
            val endRatio = ((item.maxTemp - minOverall) / tempRange).toFloat().coerceIn(0.1f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF64B5F6), Color(0xFFFFB74D), Color(0xFFFF7043))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Max temp
        Text(
            text = "${item.maxTemp.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.width(32.dp)
        )
    }
}
