package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherCondition(
    val code: Int,
    val description: String,
    val icon: ImageVector,
    val backgroundGradientDay: List<Color>,
    val backgroundGradientNight: List<Color>,
    val isRainy: Boolean = false,
    val isCloudy: Boolean = false
)

object WeatherType {
    fun fromWmo(code: Int, isDay: Boolean = true, lang: AppLanguage = AppLanguage.PT): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition(
                code = 0,
                description = when (lang) {
                    AppLanguage.PT -> if (isDay) "Céu Limpo" else "Noite Estrelada"
                    AppLanguage.ES -> if (isDay) "Cielo Despejado" else "Noche Despejada"
                    AppLanguage.EN -> if (isDay) "Clear Sky" else "Clear Night"
                },
                icon = if (isDay) Icons.Filled.WbSunny else Icons.Filled.Nightlight,
                backgroundGradientDay = listOf(Color(0xFF2980B9), Color(0xFF6DD5FA), Color(0xFFFFFFFF)),
                backgroundGradientNight = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
            )
            1, 2 -> WeatherCondition(
                code = code,
                description = when (lang) {
                    AppLanguage.PT -> if (code == 1) "Poucas Nuvens" else "Parcialmente Nublado"
                    AppLanguage.ES -> if (code == 1) "Mayormente Despejado" else "Parcialmente Nublado"
                    AppLanguage.EN -> if (code == 1) "Mostly Clear" else "Partly Cloudy"
                },
                icon = if (isDay) Icons.Filled.LightMode else Icons.Filled.WbCloudy,
                backgroundGradientDay = listOf(Color(0xFF3A7BD5), Color(0xFF3A6073), Color(0xFFE0EAFC)),
                backgroundGradientNight = listOf(Color(0xFF141E30), Color(0xFF243B55))
            )
            3 -> WeatherCondition(
                code = 3,
                description = when (lang) {
                    AppLanguage.PT -> "Nublado"
                    AppLanguage.ES -> "Nublado"
                    AppLanguage.EN -> "Overcast"
                },
                icon = Icons.Filled.Cloud,
                backgroundGradientDay = listOf(Color(0xFF536976), Color(0xFF292E49)),
                backgroundGradientNight = listOf(Color(0xFF1A1C29), Color(0xFF2C3E50)),
                isCloudy = true
            )
            45, 48 -> WeatherCondition(
                code = code,
                description = when (lang) {
                    AppLanguage.PT -> "Nevoeiro / Neblina"
                    AppLanguage.ES -> "Niebla / Neblina"
                    AppLanguage.EN -> "Fog / Mist"
                },
                icon = Icons.Filled.Air,
                backgroundGradientDay = listOf(Color(0xFF757F9A), Color(0xFFD7DDE8)),
                backgroundGradientNight = listOf(Color(0xFF232526), Color(0xFF414345)),
                isCloudy = true
            )
            51, 53, 55 -> WeatherCondition(
                code = code,
                description = when (lang) {
                    AppLanguage.PT -> "Garoa Fina"
                    AppLanguage.ES -> "Llovizna Fina"
                    AppLanguage.EN -> "Light Drizzle"
                },
                icon = Icons.Filled.Grain,
                backgroundGradientDay = listOf(Color(0xFF4B6CB7), Color(0xFF182848)),
                backgroundGradientNight = listOf(Color(0xFF16222F), Color(0xFF1E3C72)),
                isRainy = true
            )
            61, 63, 65 -> WeatherCondition(
                code = code,
                description = when (code) {
                    61 -> when (lang) {
                        AppLanguage.PT -> "Chuva Leve"
                        AppLanguage.ES -> "Lluvia Ligera"
                        AppLanguage.EN -> "Light Rain"
                    }
                    63 -> when (lang) {
                        AppLanguage.PT -> "Chuva Moderada"
                        AppLanguage.ES -> "Lluvia Moderada"
                        AppLanguage.EN -> "Moderate Rain"
                    }
                    else -> when (lang) {
                        AppLanguage.PT -> "Chuva Forte"
                        AppLanguage.ES -> "Lluvia Fuerte"
                        AppLanguage.EN -> "Heavy Rain"
                    }
                },
                icon = Icons.Filled.WaterDrop,
                backgroundGradientDay = listOf(Color(0xFF2C3E50), Color(0xFF3498DB), Color(0xFF2980B9)),
                backgroundGradientNight = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77)),
                isRainy = true
            )
            71, 73, 75, 77 -> WeatherCondition(
                code = code,
                description = when (lang) {
                    AppLanguage.PT -> "Precipitação de Neve"
                    AppLanguage.ES -> "Nevada"
                    AppLanguage.EN -> "Snowfall"
                },
                icon = Icons.Filled.Grain,
                backgroundGradientDay = listOf(Color(0xFF83A4D4), Color(0xFFB6FBFF)),
                backgroundGradientNight = listOf(Color(0xFF1F1C2C), Color(0xFF928DAB))
            )
            80, 81, 82 -> WeatherCondition(
                code = code,
                description = when (lang) {
                    AppLanguage.PT -> "Pancadas de Chuva"
                    AppLanguage.ES -> "Chubascos"
                    AppLanguage.EN -> "Rain Showers"
                },
                icon = Icons.Filled.WaterDrop,
                backgroundGradientDay = listOf(Color(0xFF1E3C72), Color(0xFF2A5298)),
                backgroundGradientNight = listOf(Color(0xFF0F2027), Color(0xFF203A43)),
                isRainy = true
            )
            95, 96, 99 -> WeatherCondition(
                code = code,
                description = when (code) {
                    95 -> when (lang) {
                        AppLanguage.PT -> "Tempestade com Raios"
                        AppLanguage.ES -> "Tormenta Eléctrica"
                        AppLanguage.EN -> "Thunderstorm"
                    }
                    else -> when (lang) {
                        AppLanguage.PT -> "Tempestade com Granizo"
                        AppLanguage.ES -> "Tormenta con Granizo"
                        AppLanguage.EN -> "Thunderstorm with Hail"
                    }
                },
                icon = Icons.Filled.Thunderstorm,
                backgroundGradientDay = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E)),
                backgroundGradientNight = listOf(Color(0xFF0A0817), Color(0xFF1B142E), Color(0xFF16192E)),
                isRainy = true
            )
            else -> WeatherCondition(
                code = code,
                description = when (lang) {
                    AppLanguage.PT -> "Parcialmente Ensolarado"
                    AppLanguage.ES -> "Parcialmente Soleado"
                    AppLanguage.EN -> "Partly Sunny"
                },
                icon = Icons.Filled.WbCloudy,
                backgroundGradientDay = listOf(Color(0xFF3A7BD5), Color(0xFF00D2FF)),
                backgroundGradientNight = listOf(Color(0xFF141E30), Color(0xFF243B55))
            )
        }
    }
}
