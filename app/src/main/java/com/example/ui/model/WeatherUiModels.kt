package com.example.ui.model

import com.example.data.api.GeocodingResultDto

data class HourlyForecastItem(
    val timeLabel: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val isDay: Boolean = true
)

data class DailyForecastItem(
    val dateLabel: String,
    val dayOfWeek: String,
    val weatherCode: Int,
    val minTemp: Double,
    val maxTemp: Double,
    val precipitationProbability: Int,
    val uvIndex: Double,
    val sunrise: String,
    val sunset: String
)

data class WeatherDetails(
    val cityName: String,
    val stateCountry: String,
    val latitude: Double,
    val longitude: Double,
    val currentTemp: Double,
    val feelsLike: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val conditionCode: Int,
    val isDay: Boolean,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Double,
    val uvIndex: Double,
    val precipitationProbability: Int,
    val sunrise: String,
    val sunset: String,
    val hourlyList: List<HourlyForecastItem>,
    val dailyList: List<DailyForecastItem>,
    val aiInsight: String = ""
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val weatherData: WeatherDetails? = null,
    val error: String? = null,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<GeocodingResultDto> = emptyList(),
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Olá! Sou o Clima AI. Posso te ajudar a escolher roupas para o dia, planejar atividades ao ar livre ou responder qualquer dúvida sobre a previsão.",
            isUser = false
        )
    ),
    val isAiLoading: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val selectedLanguage: AppLanguage = AppLanguage.PT
)
