package com.example.data.api

import com.squareup.moshi.Json

data class WeatherResponse(
    @field:Json(name = "latitude") val latitude: Double? = null,
    @field:Json(name = "longitude") val longitude: Double? = null,
    @field:Json(name = "timezone") val timezone: String? = null,
    @field:Json(name = "current_weather") val currentWeather: CurrentWeatherDto? = null,
    @field:Json(name = "hourly") val hourly: HourlyDto? = null,
    @field:Json(name = "daily") val daily: DailyDto? = null
)

data class CurrentWeatherDto(
    @field:Json(name = "temperature") val temperature: Double = 0.0,
    @field:Json(name = "windspeed") val windspeed: Double = 0.0,
    @field:Json(name = "winddirection") val winddirection: Double = 0.0,
    @field:Json(name = "weathercode") val weathercode: Int = 0,
    @field:Json(name = "is_day") val isDay: Int = 1,
    @field:Json(name = "time") val time: String = ""
)

data class HourlyDto(
    @field:Json(name = "time") val time: List<String>? = null,
    @field:Json(name = "temperature_2m") val temperatures: List<Double>? = null,
    @field:Json(name = "relative_humidity_2m") val humidities: List<Int>? = null,
    @field:Json(name = "apparent_temperature") val apparentTemperatures: List<Double>? = null,
    @field:Json(name = "precipitation_probability") val precipitationProbabilities: List<Int>? = null,
    @field:Json(name = "weather_code") val weatherCodes: List<Int>? = null,
    @field:Json(name = "wind_speed_10m") val windSpeeds: List<Double>? = null
)

data class DailyDto(
    @field:Json(name = "time") val time: List<String>? = null,
    @field:Json(name = "weather_code") val weatherCodes: List<Int>? = null,
    @field:Json(name = "temperature_2m_max") val maxTemperatures: List<Double>? = null,
    @field:Json(name = "temperature_2m_min") val minTemperatures: List<Double>? = null,
    @field:Json(name = "apparent_temperature_max") val apparentMax: List<Double>? = null,
    @field:Json(name = "apparent_temperature_min") val apparentMin: List<Double>? = null,
    @field:Json(name = "sunrise") val sunrises: List<String>? = null,
    @field:Json(name = "sunset") val sunsets: List<String>? = null,
    @field:Json(name = "uv_index_max") val uvIndices: List<Double>? = null,
    @field:Json(name = "precipitation_probability_max") val precipitationProbabilities: List<Int>? = null
)

data class GeocodingResponse(
    @field:Json(name = "results") val results: List<GeocodingResultDto>? = null
)

data class GeocodingResultDto(
    @field:Json(name = "id") val id: Long? = null,
    @field:Json(name = "name") val name: String = "",
    @field:Json(name = "latitude") val latitude: Double = 0.0,
    @field:Json(name = "longitude") val longitude: Double = 0.0,
    @field:Json(name = "country") val country: String? = null,
    @field:Json(name = "admin1") val admin1: String? = null
)
