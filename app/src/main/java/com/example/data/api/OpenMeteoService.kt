package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Interface Retrofit que define os contratos de comunicação HTTP com os endpoints públicos da Open-Meteo.
 *
 * Utilizada para obter:
 * - Previsões numéricas do tempo (temperatura, umidade, vento, UV, nascer/pôr do sol).
 * - Geocodificação reversa e pesquisa de cidades globais com autocompletação.
 */
interface OpenMeteoService {

    /**
     * Consulta a previsão meteorológica detalhada para coordenadas geográficas de latitude e longitude.
     *
     * @param latitude Coordenada de latitude (ex: -23.5505).
     * @param longitude Coordenada de longitude (ex: -46.6333).
     * @param currentWeather Se deve retornar a condição climática em tempo real no nó `current_weather`.
     * @param hourly Lista de variáveis horárias separadas por vírgula.
     * @param daily Lista de variáveis diárias agregadas separadas por vírgula.
     * @param timezone Fuso horário de referência ("auto" detecta automaticamente com base na posição).
     * @return [WeatherResponse] com a estrutura tipada retornada pela API.
     */
    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse

    /**
     * Pesquisa cidades globais a partir de uma consulta textual (geocodificação direta).
     *
     * @param name Nome parcial ou completo da cidade pesquisada.
     * @param count Quantidade máxima de resultados retornados (padrão 10).
     * @param language Código ISO do idioma para os nomes de cidades e países ("pt", "es", "en").
     * @param format Formato de saída da resposta ("json").
     * @return [GeocodingResponse] contendo a lista de correspondências geográficas encontradas.
     */
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "pt",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

/**
 * Provedor Singleton do cliente Retrofit e Moshi configurados com timeouts e interceptadores de log.
 */
object ApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    /** Instância lazy do serviço [OpenMeteoService] */
    val openMeteoService: OpenMeteoService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoService::class.java)
    }
}
