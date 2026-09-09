package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.GeocodingResultDto
import com.example.data.api.GeminiService
import com.example.data.api.OpenMeteoService
import com.example.data.db.SavedCityDao
import com.example.data.db.SavedCityEntity
import com.example.ui.model.AppLanguage
import com.example.ui.model.DailyForecastItem
import com.example.ui.model.HourlyForecastItem
import com.example.ui.model.WeatherDetails
import com.example.ui.util.WeatherLocalization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositório central de dados climáticos (Single Source of Truth).
 *
 * Responsável por orquestrar:
 * - Chamadas REST à API de previsão e geocodificação da Open-Meteo ([OpenMeteoService]).
 * - Persistência offline e gerenciamento de cidades salvas no Room ([SavedCityDao]).
 * - Geração de insights meteorológicos e respostas conversacionais inteligentes com [GeminiService].
 * - Conversão de modelos brutos da rede em estruturas ricas e tipadas para a camada de UI ([WeatherDetails]).
 *
 * Todas as operações de I/O são despachadas com segurança em [Dispatchers.IO].
 *
 * @param api Serviço de comunicação HTTP com a Open-Meteo.
 * @param geminiService Serviço de inferência de IA usando o modelo Gemini.
 * @param savedCityDao DAO para persistência local de cidades no Room Database.
 */
class WeatherRepository(
    private val api: OpenMeteoService = ApiClient.openMeteoService,
    private val geminiService: GeminiService = GeminiService(),
    private val savedCityDao: SavedCityDao
) {
    /** Fluxo reativo contendo todas as cidades favoritadas/salvas localmente */
    val savedCities: Flow<List<SavedCityEntity>> = savedCityDao.getAllSavedCities()

    /**
     * Popula o banco local com cidades de exemplo caso seja a primeira execução do aplicativo.
     */
    suspend fun initDefaultCitiesIfEmpty() = withContext(Dispatchers.IO) {
        if (savedCityDao.getCount() == 0) {
            val defaults = listOf(
                SavedCityEntity(name = "São Paulo", state = "São Paulo", country = "Brasil", latitude = -23.5505, longitude = -46.6333, isFavorite = true),
                SavedCityEntity(name = "Rio de Janeiro", state = "Rio de Janeiro", country = "Brasil", latitude = -22.9068, longitude = -43.1729, isFavorite = true),
                SavedCityEntity(name = "Buenos Aires", state = "Buenos Aires", country = "Argentina", latitude = -34.6037, longitude = -58.3816, isFavorite = true),
                SavedCityEntity(name = "Madrid", state = "Comunidad de Madrid", country = "España", latitude = 40.4168, longitude = -3.7038, isFavorite = true),
                SavedCityEntity(name = "Brasília", state = "Distrito Federal", country = "Brasil", latitude = -15.7975, longitude = -47.8919, isFavorite = true)
            )
            defaults.forEach { savedCityDao.insertCity(it) }
        }
    }

    /**
     * Consulta a previsão meteorológica completa para as coordenadas indicadas e compila para a UI.
     *
     * @param latitude Coordenada de latitude geográfica.
     * @param longitude Coordenada de longitude geográfica.
     * @param cityName Nome de exibição da cidade.
     * @param stateCountry Estado e país formatados para exibição.
     * @param lang Idioma selecionado para localização de dias e resumos ([AppLanguage]).
     * @return [WeatherDetails] completo contendo clima atual, 24h horárias e 7 dias de previsão.
     */
    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
        stateCountry: String,
        lang: AppLanguage = AppLanguage.PT
    ): WeatherDetails = withContext(Dispatchers.IO) {
        val response = api.getForecast(latitude = latitude, longitude = longitude)

        val current = response.currentWeather
        val currentTemp = current?.temperature ?: 0.0
        val windSpeed = current?.windspeed ?: 0.0
        val windDir = current?.winddirection ?: 0.0
        val code = current?.weathercode ?: 0
        val isDay = (current?.isDay ?: 1) == 1

        val hourlyList = mutableListOf<HourlyForecastItem>()
        val hourly = response.hourly
        if (hourly != null && !hourly.time.isNullOrEmpty() && !hourly.temperatures.isNullOrEmpty()) {
            val times = hourly.time
            val temps = hourly.temperatures
            val codes = hourly.weatherCodes ?: emptyList()
            val precips = hourly.precipitationProbabilities ?: emptyList()

            val nowIsoPrefix = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US).format(Date())
            var startIndex = times.indexOfFirst { it.startsWith(nowIsoPrefix) }
            if (startIndex < 0) startIndex = 0

            val count = minOf(24, times.size - startIndex)
            for (i in 0 until count) {
                val idx = startIndex + i
                val rawTime = times[idx]
                val hourStr = try {
                    val hour = rawTime.substringAfter("T").substringBefore(":")
                    if (i == 0) WeatherLocalization.getNowLabel(lang) else "${hour}h"
                } catch (e: Exception) {
                    rawTime
                }
                val hourInt = try { rawTime.substringAfter("T").substringBefore(":").toInt() } catch (e: Exception) { 12 }
                val isHourDay = hourInt in 6..18

                hourlyList.add(
                    HourlyForecastItem(
                        timeLabel = hourStr,
                        temperature = temps.getOrNull(idx) ?: currentTemp,
                        weatherCode = codes.getOrNull(idx) ?: code,
                        precipitationProbability = precips.getOrNull(idx) ?: 0,
                        isDay = isHourDay
                    )
                )
            }
        }

        val dailyList = mutableListOf<DailyForecastItem>()
        val daily = response.daily
        var minToday = currentTemp - 2.0
        var maxToday = currentTemp + 4.0
        var sunriseToday = "06:00"
        var sunsetToday = "18:00"
        var uvToday = 5.0
        var precipProbToday = 10
        var feelsLikeToday = currentTemp

        if (daily != null && !daily.time.isNullOrEmpty()) {
            val dates = daily.time
            val maxTemps = daily.maxTemperatures ?: emptyList()
            val minTemps = daily.minTemperatures ?: emptyList()
            val codes = daily.weatherCodes ?: emptyList()
            val precips = daily.precipitationProbabilities ?: emptyList()
            val uvs = daily.uvIndices ?: emptyList()
            val sunrises = daily.sunrises ?: emptyList()
            val sunsets = daily.sunsets ?: emptyList()
            val apparentMax = daily.apparentMax ?: emptyList()

            for (i in dates.indices) {
                val dateStr = dates[i]
                val dMax = maxTemps.getOrNull(i) ?: currentTemp
                val dMin = minTemps.getOrNull(i) ?: currentTemp
                val dCode = codes.getOrNull(i) ?: code
                val dPrecip = precips.getOrNull(i) ?: 0
                val dUv = uvs.getOrNull(i) ?: 4.0
                val dSunrise = sunrises.getOrNull(i)?.substringAfter("T") ?: "06:15"
                val dSunset = sunsets.getOrNull(i)?.substringAfter("T") ?: "18:05"

                val dayLabel = WeatherLocalization.getDayLabel(i, dateStr, lang)

                if (i == 0) {
                    minToday = dMin
                    maxToday = dMax
                    sunriseToday = dSunrise
                    sunsetToday = dSunset
                    uvToday = dUv
                    precipProbToday = dPrecip
                    feelsLikeToday = apparentMax.getOrNull(0) ?: currentTemp
                }

                dailyList.add(
                    DailyForecastItem(
                        dateLabel = dateStr,
                        dayOfWeek = dayLabel,
                        weatherCode = dCode,
                        minTemp = dMin,
                        maxTemp = dMax,
                        precipitationProbability = dPrecip,
                        uvIndex = dUv,
                        sunrise = dSunrise,
                        sunset = dSunset
                    )
                )
            }
        }

        // Umidade relativa referente à hora corrente
        val humidity = response.hourly?.humidities?.firstOrNull() ?: 65

        // Síntese rápida e humanizada gerada para o cartão inicial
        val aiSummary = generateQuickSummary(currentTemp, feelsLikeToday, code, precipProbToday, lang)

        WeatherDetails(
            cityName = cityName,
            stateCountry = stateCountry,
            latitude = latitude,
            longitude = longitude,
            currentTemp = currentTemp,
            feelsLike = feelsLikeToday,
            minTemp = minToday,
            maxTemp = maxToday,
            conditionCode = code,
            isDay = isDay,
            humidity = humidity,
            windSpeed = windSpeed,
            windDirection = windDir,
            uvIndex = uvToday,
            precipitationProbability = precipProbToday,
            sunrise = sunriseToday,
            sunset = sunsetToday,
            hourlyList = hourlyList,
            dailyList = dailyList,
            aiInsight = aiSummary
        )
    }

    /**
     * Gera uma dica prática e humanizada no idioma ativo para exibição no cartão de destaque.
     */
    private fun generateQuickSummary(
        temp: Double,
        feelsLike: Double,
        code: Int,
        precipProb: Int,
        lang: AppLanguage
    ): String {
        return when (lang) {
            AppLanguage.ES -> when {
                code in listOf(95, 96, 99) -> "Alerta de tormenta: ojo con los rayos, llevá paraguas o mejor quedate bajo techo."
                code in listOf(61, 63, 65, 80, 81, 82) -> "Lluvia prevista para hoy ($precipProb% de probabilidad). No te olvides el paraguas al salir."
                temp >= 28 -> "Día bien soleado y caluroso ($temp°C). Tomá mucha agua y cuidate del sol."
                temp <= 14 -> "Día fresco ($temp°C, sensación de $feelsLike°C). Abrigáte bien para salir."
                else -> "Clima muy agradable y disfrutable para hacer cosas al aire libre hoy."
            }
            AppLanguage.EN -> when {
                code in listOf(95, 96, 99) -> "Storm alert: watch out for lightning and stay in a safe, covered spot."
                code in listOf(61, 63, 65, 80, 81, 82) -> "Rain expected today ($precipProb% chance). Don't leave your umbrella behind!"
                temp >= 28 -> "Sunny and hot day ($temp°C). Stay hydrated and wear sun protection."
                temp <= 14 -> "Chilly day ($temp°C, feels like $feelsLike°C). Grab a cozy jacket before heading out."
                else -> "Very pleasant weather today for outdoor strolls and activities."
            }
            AppLanguage.PT -> when {
                code in listOf(95, 96, 99) -> "Alerta de tempestade: evite locais abertos e fique em um lugar seguro."
                code in listOf(61, 63, 65, 80, 81, 82) -> "Chuva presente hoje ($precipProb% de chance). Não esqueça o guarda-chuva ao sair!"
                temp >= 28 -> "Dia ensolarado e quente ($temp°C). Beba bastante água e use protetor solar."
                temp <= 14 -> "Dia friozinho ($temp°C, sensação de $feelsLike°C). Vista um bom agasalho confortável."
                else -> "Condições super agradáveis para seus compromissos e passeios ao ar livre hoje."
            }
        }
    }

    /**
     * Pesquisa cidades globais através da API de geocodificação da Open-Meteo.
     *
     * @param query Termo de busca digitado pelo usuário (mínimo de 2 caracteres).
     * @param lang Código do idioma para tradução de nomes e regiões.
     * @return Lista de cidades encontradas com suas coordenadas geográficas.
     */
    suspend fun searchCities(query: String, lang: AppLanguage = AppLanguage.PT): List<GeocodingResultDto> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        try {
            val res = api.searchCities(name = query.trim(), language = lang.code)
            res.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Salva uma cidade no Room Database */
    suspend fun saveCity(city: SavedCityEntity) = withContext(Dispatchers.IO) {
        savedCityDao.insertCity(city)
    }

    /** Remove uma cidade do Room Database */
    suspend fun removeCity(city: SavedCityEntity) = withContext(Dispatchers.IO) {
        savedCityDao.deleteCity(city)
    }

    /**
     * Realiza uma consulta ao assistente inteligente com contexto meteorológico e histórico da conversa.
     *
     * @param weather Dados da cidade ativa.
     * @param query Dúvida ou mensagem enviada pelo usuário.
     * @param history Histórico recente de perguntas e respostas.
     * @param lang Idioma para a resposta do assistente.
     * @return Resposta humanizada e contextualizada.
     */
    suspend fun askAi(
        weather: WeatherDetails,
        query: String,
        history: List<Pair<String, String>>,
        lang: AppLanguage = AppLanguage.PT
    ): String {
        val conditionDesc = com.example.ui.model.WeatherType.fromWmo(weather.conditionCode, weather.isDay, lang).description
        return geminiService.askWeatherAssistant(
            cityName = weather.cityName,
            currentTemp = weather.currentTemp,
            condition = conditionDesc,
            feelsLike = weather.feelsLike,
            humidity = weather.humidity,
            windSpeed = weather.windSpeed,
            userQuery = query,
            chatHistory = history,
            lang = lang
        )
    }
}
