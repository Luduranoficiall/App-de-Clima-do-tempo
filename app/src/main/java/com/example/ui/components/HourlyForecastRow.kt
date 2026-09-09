package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AppLanguage
import com.example.ui.model.HourlyForecastItem
import com.example.ui.model.WeatherType
import com.example.ui.util.WeatherLocalization
import kotlin.math.roundToInt

@Composable
fun HourlyForecastRow(
    hourlyList: List<HourlyForecastItem>,
    selectedLanguage: AppLanguage = AppLanguage.PT,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

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
            .padding(vertical = 16.dp)
            .testTag("hourly_forecast_container")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = WeatherLocalization.getHourlyHeader(selectedLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val nowLabel = WeatherLocalization.getNowLabel(selectedLanguage)

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(hourlyList) { item ->
                HourlyItemCard(
                    item = item,
                    selectedLanguage = selectedLanguage,
                    nowLabel = nowLabel
                )
            }
        }
    }
}

@Composable
private fun HourlyItemCard(
    item: HourlyForecastItem,
    selectedLanguage: AppLanguage,
    nowLabel: String
) {
    val condition = WeatherType.fromWmo(item.weatherCode, item.isDay, selectedLanguage)
    val isNow = item.timeLabel == "Agora" || item.timeLabel == "Ahora" || item.timeLabel == "Now"

    Column(
        modifier = Modifier
            .width(64.dp)
            .background(
                color = if (isNow) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = if (isNow) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isNow) nowLabel else item.timeLabel,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isNow) FontWeight.Bold else FontWeight.Medium,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            imageVector = condition.icon,
            contentDescription = condition.description,
            tint = if (item.isDay) Color(0xFFFFEB3B) else Color(0xFFB2EBF2),
            modifier = Modifier.size(26.dp)
        )

        if (item.precipitationProbability > 15) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF81D4FA),
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "${item.precipitationProbability}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = Color(0xFF81D4FA),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${item.temperature.roundToInt()}°",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}
