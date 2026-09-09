package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AppLanguage
import com.example.ui.model.WeatherDetails
import com.example.ui.model.WeatherType
import kotlin.math.roundToInt

@Composable
fun CurrentWeatherCard(
    weather: WeatherDetails,
    isFavorite: Boolean,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAiChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val condition = WeatherType.fromWmo(weather.conditionCode, weather.isDay, selectedLanguage)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row: Location & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenSearch() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("location_selector_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Localização",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = weather.cityName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                    if (weather.stateCountry.isNotBlank()) {
                        Text(
                            text = weather.stateCountry,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("favorite_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar Cidade",
                        tint = if (isFavorite) Color(0xFFFF5252) else Color.White
                    )
                }
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("search_city_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar Cidade",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Language Selector Switcher
        LanguageSelectorRow(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = onLanguageSelected,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Large Condition Icon with glowing backdrop
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = condition.icon,
                contentDescription = condition.description,
                tint = if (weather.isDay) Color(0xFFFFEB3B) else Color(0xFFE0F7FA),
                modifier = Modifier.size(68.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Big Main Temperature
        Text(
            text = "${weather.currentTemp.roundToInt()}°",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 84.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            ),
            modifier = Modifier.testTag("current_temperature_text")
        )

        // Weather condition description
        Text(
            text = condition.description,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // High / Low and Feels Like Row
        val maxLabel = when (selectedLanguage) {
            AppLanguage.ES -> "Máx"
            AppLanguage.EN -> "High"
            AppLanguage.PT -> "Máx"
        }
        val minLabel = when (selectedLanguage) {
            AppLanguage.ES -> "Mín"
            AppLanguage.EN -> "Low"
            AppLanguage.PT -> "Mín"
        }
        val feelsLabel = when (selectedLanguage) {
            AppLanguage.ES -> "Sensación"
            AppLanguage.EN -> "Feels like"
            AppLanguage.PT -> "Sensação"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$maxLabel: ${weather.maxTemp.roundToInt()}°  •  $minLabel: ${weather.minTemp.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "•  $feelsLabel: ${weather.feelsLike.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f)
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // AI Insight Banner (interactive pill)
        if (weather.aiInsight.isNotBlank()) {
            val bannerTitle = when (selectedLanguage) {
                AppLanguage.ES -> "Consejo Inteligente • Clima AI"
                AppLanguage.EN -> "Smart Advice • Clima AI"
                AppLanguage.PT -> "Dica Inteligente • Clima AI"
            }

            Surface(
                onClick = onOpenAiChat,
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.18f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                    .testTag("ai_insight_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Clima AI",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bannerTitle,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFFFFE082),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = weather.aiInsight,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
