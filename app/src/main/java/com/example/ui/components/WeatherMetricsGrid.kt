package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AppLanguage
import com.example.ui.model.WeatherDetails
import com.example.ui.util.WeatherLocalization
import kotlin.math.roundToInt

@Composable
fun WeatherMetricsGrid(
    weather: WeatherDetails,
    selectedLanguage: AppLanguage = AppLanguage.PT,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Sensação Térmica & Umidade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = WeatherLocalization.getFeelsLikeLabel(selectedLanguage),
                value = "${weather.feelsLike.roundToInt()}°",
                subtitle = WeatherLocalization.getFeelsLikeSubtitle(weather.feelsLike, weather.currentTemp, selectedLanguage),
                icon = Icons.Default.Thermostat,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = WeatherLocalization.getHumidityLabel(selectedLanguage),
                value = "${weather.humidity}%",
                subtitle = WeatherLocalization.getHumiditySubtitle(weather.humidity, selectedLanguage),
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Vento & Índice UV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val windDirectionCardinal = WeatherLocalization.getWindDirectionCardinal(weather.windDirection, selectedLanguage)
            val dirPrefix = when (selectedLanguage) {
                AppLanguage.ES -> "Dirección: "
                AppLanguage.EN -> "Direction: "
                AppLanguage.PT -> "Direção: "
            }

            MetricCard(
                title = WeatherLocalization.getWindLabel(selectedLanguage),
                value = "${weather.windSpeed.roundToInt()} km/h",
                subtitle = "$dirPrefix$windDirectionCardinal",
                icon = Icons.Default.Air,
                modifier = Modifier.weight(1f)
            )

            val uvCategory = WeatherLocalization.getUvCategory(weather.uvIndex, selectedLanguage)
            val riskWord = when (selectedLanguage) {
                AppLanguage.ES -> "riesgo"
                AppLanguage.EN -> "risk"
                AppLanguage.PT -> "risco"
            }

            MetricCard(
                title = WeatherLocalization.getUvLabel(selectedLanguage),
                value = "${weather.uvIndex.roundToInt()}",
                subtitle = "$uvCategory $riskWord",
                icon = Icons.Default.WbSunny,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Nascer e Pôr do Sol
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sunriseSub = when (selectedLanguage) {
                AppLanguage.ES -> "Amanecer del día"
                AppLanguage.EN -> "Morning dawn"
                AppLanguage.PT -> "Aurora matinal"
            }
            val sunsetSub = when (selectedLanguage) {
                AppLanguage.ES -> "Crepúsculo"
                AppLanguage.EN -> "Twilight dusk"
                AppLanguage.PT -> "Crepúsculo"
            }

            MetricCard(
                title = WeatherLocalization.getSunriseLabel(selectedLanguage),
                value = weather.sunrise,
                subtitle = sunriseSub,
                icon = Icons.Default.WbSunny,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = WeatherLocalization.getSunsetLabel(selectedLanguage),
                value = weather.sunset,
                subtitle = sunsetSub,
                icon = Icons.Default.WbTwilight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.22f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .testTag("metric_${title.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.75f)
            )
        )
    }
}
