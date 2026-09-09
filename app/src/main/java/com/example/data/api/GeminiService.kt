package com.example.data.api

import com.example.BuildConfig
import com.example.ui.model.AppLanguage
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Estrutura de dados para o corpo da requisição enviada à Google Gemini REST API.
 *
 * @property contents Lista de mensagens que compõem o histórico e a mensagem atual.
 * @property systemInstruction Instrução de sistema configurando a persona humana do assistente.
 */
data class GeminiRequestDto(
    @field:Json(name = "contents") val contents: List<GeminiContentDto>,
    @field:Json(name = "systemInstruction") val systemInstruction: GeminiContentDto? = null
)

/**
 * Representação de uma mensagem no formato Gemini (role: "user" | "model").
 *
 * @property parts Blocos de texto que compõem a mensagem.
 * @property role Papel do emissor na conversa.
 */
data class GeminiContentDto(
    @field:Json(name = "parts") val parts: List<GeminiPartDto>,
    @field:Json(name = "role") val role: String? = null
)

/**
 * Parte individual de texto dentro do conteúdo da mensagem Gemini.
 *
 * @property text Conteúdo textual enviado ou retornado.
 */
data class GeminiPartDto(
    @field:Json(name = "text") val text: String
)

/**
 * DTO de resposta serializado pela API do Gemini.
 *
 * @property candidates Lista de respostas candidatas geradas pelo modelo de IA.
 */
data class GeminiResponseDto(
    @field:Json(name = "candidates") val candidates: List<GeminiCandidateDto>? = null
)

/**
 * Candidato retornado contendo o conteúdo gerado pela IA.
 *
 * @property content Conteúdo gerado contendo texto e metadados.
 */
data class GeminiCandidateDto(
    @field:Json(name = "content") val content: GeminiContentDto? = null
)

/**
 * Serviço responsável pela comunicação com o Google Gemini e geração de conselhos meteorológicos humanizados.
 *
 * Destaques de engenharia:
 * - Utiliza o modelo `gemini-3.5-flash` para baixa latência e alta precisão contextual.
 * - Injeta dinamicamente instruções de sistema adaptadas ao idioma ([AppLanguage.PT], [AppLanguage.ES], [AppLanguage.EN]).
 * - Possui um **Motor de Fallback Local** ([generateLocalHumanAdvice]) que garante respostas acolhedoras e úteis
 *   mesmo quando o dispositivo estiver sem conexão à internet ou sem chave de API configurada.
 */
class GeminiService {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val requestAdapter = moshi.adapter(GeminiRequestDto::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponseDto::class.java)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    /**
     * Envia uma mensagem para o assistente Clima AI e retorna a resposta formatada.
     *
     * @param cityName Nome da cidade atualmente em exibição.
     * @param currentTemp Temperatura atual medida em graus Celsius.
     * @param condition Descrição textual da condição do tempo (ex: "Chuva Moderada", "Cielo Despejado").
     * @param feelsLike Sensação térmica em graus Celsius.
     * @param humidity Percentual de umidade relativa do ar.
     * @param windSpeed Velocidade do vento em km/h.
     * @param userQuery Pergunta ou comentário enviado pelo usuário.
     * @param chatHistory Histórico recente da conversa para manter a coerência do diálogo.
     * @param lang Idioma em que o assistente deve responder.
     * @return Texto de resposta com tom acolhedor e humanizado.
     */
    suspend fun askWeatherAssistant(
        cityName: String,
        currentTemp: Double,
        condition: String,
        feelsLike: Double,
        humidity: Int,
        windSpeed: Double,
        userQuery: String,
        chatHistory: List<Pair<String, String>> = emptyList(),
        lang: AppLanguage = AppLanguage.PT
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalHumanAdvice(
                cityName, currentTemp, condition, feelsLike, humidity, windSpeed, userQuery, lang
            )
        }

        try {
            val systemPromptText = when (lang) {
                AppLanguage.ES -> "Eres un amigo muy cercano, humano, empático y atento que sabe mucho del clima. " +
                        "Estás conversando con alguien en la ciudad de $cityName. Datos meteorológicos actuales: Temperatura: $currentTemp°C, Condición: $condition, " +
                        "Sensación térmica: $feelsLike°C, Humedad: $humidity%, Viento: $windSpeed km/h. " +
                        "Responde de manera humana, espontánea, cálida y útil en español. Evita sonar como un robot o una máquina fría."
                AppLanguage.EN -> "You are a warm, friendly, thoughtful human who is great at talking about the weather. " +
                        "You are chatting with someone in $cityName. Current weather data: Temp: $currentTemp°C, Condition: $condition, " +
                        "Feels like: $feelsLike°C, Humidity: $humidity%, Wind: $windSpeed km/h. " +
                        "Respond naturally like a caring friend in English, giving real-world, practical advice without sounding robotic."
                AppLanguage.PT -> "Você é um amigo próximo, muito simpático, atencioso e humano, que entende muito de clima e bem-estar. " +
                        "Você está conversando com alguém na cidade de $cityName. Dados atuais: Temperatura: $currentTemp°C, Condição: $condition, " +
                        "Sensação térmica: $feelsLike°C, Umidade: $humidity%, Vento: $windSpeed km/h. " +
                        "Responda de forma espontânea, humana, acolhedora e prática em português do Brasil, como se estivesse conversando pessoalmente. Evite respostas robóticas ou frias."
            }

            val systemInstruction = GeminiContentDto(
                parts = listOf(GeminiPartDto(text = systemPromptText))
            )

            val contents = mutableListOf<GeminiContentDto>()
            for ((role, text) in chatHistory) {
                contents.add(
                    GeminiContentDto(
                        role = if (role == "user") "user" else "model",
                        parts = listOf(GeminiPartDto(text = text))
                    )
                )
            }
            contents.add(
                GeminiContentDto(
                    role = "user",
                    parts = listOf(GeminiPartDto(text = userQuery))
                )
            )

            val requestBodyJson = requestAdapter.toJson(
                GeminiRequestDto(
                    contents = contents,
                    systemInstruction = systemInstruction
                )
            )

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext generateLocalHumanAdvice(
                    cityName, currentTemp, condition, feelsLike, humidity, windSpeed, userQuery, lang
                )
            }

            val parsed = responseAdapter.fromJson(responseBody)
            val answer = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!answer.isNullOrBlank()) {
                answer.trim()
            } else {
                generateLocalHumanAdvice(
                    cityName, currentTemp, condition, feelsLike, humidity, windSpeed, userQuery, lang
                )
            }
        } catch (e: Exception) {
            generateLocalHumanAdvice(
                cityName, currentTemp, condition, feelsLike, humidity, windSpeed, userQuery, lang
            )
        }
    }

    /**
     * Motor de heurística local para gerar conselhos humanizados quando offline ou sem API Key.
     *
     * Avalia palavras-chave e limiares de temperatura para sugerir roupas, precauções com chuva
     * e práticas esportivas de maneira natural e acolhedora.
     */
    private fun generateLocalHumanAdvice(
        cityName: String,
        temp: Double,
        condition: String,
        feelsLike: Double,
        humidity: Int,
        windSpeed: Double,
        query: String,
        lang: AppLanguage
    ): String {
        val q = query.lowercase()

        when (lang) {
            AppLanguage.ES -> {
                return when {
                    q.contains("ropa") || q.contains("vestir") || q.contains("abrigo") || q.contains("chaqueta") -> {
                        when {
                            temp < 15 -> "En $cityName está haciendo bastante frío ($temp°C). Te recomiendo ponerte un buen abrigo o campera abrigada; la sensación térmica es de $feelsLike°C."
                            temp < 22 -> "El clima en $cityName está súper templado y agradable ($temp°C). Una remera con una camperita ligera o cárdigan te vendrá perfecto."
                            else -> "¡Está hermoso y cálido en $cityName ($temp°C)! Ropa liviana de algodón, tus gafas de sol y no te olvides de tomar agua."
                        }
                    }
                    q.contains("lluvia") || q.contains("paraguas") || q.contains("llover") -> {
                        if (condition.contains("Lluvia", ignoreCase = true) || condition.contains("Chubascos", ignoreCase = true) || condition.contains("Tormenta", ignoreCase = true)) {
                            "¡Sí, llevate paraguas o impermeable! En este momento hay $condition en $cityName y podés mojarte."
                        } else {
                            "Por ahora no se ve lluvia inmediata en $cityName, el cielo está $condition. Igual siempre es bueno estar atento si el cielo se nubla."
                        }
                    }
                    q.contains("correr") || q.contains("ejercicio") || q.contains("deporte") || q.contains("entrenar") -> {
                        when {
                            condition.contains("Tormenta") -> "Mejor evitar salir al aire libre con tormenta eléctrica. Una sesión de entrenamiento en casa o gimnasio es lo más seguro hoy."
                            temp > 28 -> "Hace calor ($temp°C). Si vas a entrenar, aprovechá las primeras horas de la mañana o el atardecer, e hidratate bien."
                            else -> "¡Excelente momento para salir a moverse en $cityName! Está $condition con un viento suave de $windSpeed km/h."
                        }
                    }
                    else -> {
                        "Aquí en $cityName tenemos $temp°C con $condition, sensación de $feelsLike°C y humedad del $humidity%. ¡Que tengas un día increíble!"
                    }
                }
            }
            AppLanguage.EN -> {
                return when {
                    q.contains("wear") || q.contains("clothes") || q.contains("jacket") -> {
                        when {
                            temp < 15 -> "It's quite chilly in $cityName right now ($temp°C). Grab a cozy jacket or sweater — feels like $feelsLike°C."
                            temp < 22 -> "Weather in $cityName is mild and comfortable ($temp°C). A t-shirt with a light layer or hoodie is spot on."
                            else -> "Nice and warm in $cityName today ($temp°C)! Light breathable clothes, sunglasses, and staying hydrated are the way to go."
                        }
                    }
                    q.contains("rain") || q.contains("umbrella") -> {
                        if (condition.contains("Rain", ignoreCase = true) || condition.contains("Shower", ignoreCase = true) || condition.contains("Thunderstorm", ignoreCase = true)) {
                            "Definitely grab an umbrella or raincoat before heading out in $cityName! Looks like $condition right now."
                        } else {
                            "Looks pretty clear for rain right now in $cityName, it's currently $condition. Enjoy the day!"
                        }
                    }
                    q.contains("run") || q.contains("exercise") || q.contains("workout") -> {
                        when {
                            condition.contains("Thunderstorm") -> "Best to stay indoors for your workout today to stay safe from the storm."
                            temp > 28 -> "It's quite warm ($temp°C). If you're going for a run, early morning or dusk is best, and remember your water bottle!"
                            else -> "Great time for some outdoor activity in $cityName! It's $condition with a gentle breeze of $windSpeed km/h."
                        }
                    }
                    else -> {
                        "Currently in $cityName it's $temp°C with $condition (feels like $feelsLike°C), humidity at $humidity% and wind at $windSpeed km/h. Wishing you a great day!"
                    }
                }
            }
            AppLanguage.PT -> {
                return when {
                    q.contains("vestir") || q.contains("roupa") || q.contains("casaco") -> {
                        when {
                            temp < 15 -> "Em $cityName está bem friozinho ($temp°C). Coloca aquele casaco quentinho ou uma blusa de lã bem confortável; a sensação é de $feelsLike°C."
                            temp < 22 -> "O tempo em $cityName tá uma delícia, bem ameno ($temp°C). Uma camiseta com uma jaquetinha leve ou cardigã cai como uma luva."
                            else -> "Tá um dia lindo e quentinho em $cityName ($temp°C)! Roupa leve, óculos de sol e muita água pra se hidratar."
                        }
                    }
                    q.contains("chuva") || q.contains("guarda-chuva") || q.contains("chover") -> {
                        if (condition.contains("Chuva", ignoreCase = true) || condition.contains("Garoa", ignoreCase = true) || condition.contains("Tempestade", ignoreCase = true)) {
                            "Sim, não sai sem guarda-chuva ou capa! Tá rolando $condition por aqui em $cityName."
                        } else {
                            "Pode ficar tranquilo por enquanto, o tempo em $cityName tá $condition e não tem cara de chuva agora. Mas vale dar uma olhada no céu de vez em quando!"
                        }
                    }
                    q.contains("correr") || q.contains("exercício") || q.contains("treino") || q.contains("pedalar") -> {
                        when {
                            condition.contains("Tempestade") -> "Hoje é melhor ficar no treino indoor por segurança. Com tempestade e raios não vale a pena arriscar."
                            temp > 28 -> "Tá quente ($temp°C)! Se for treinar, prefira o comecinho da manhã ou o fim de tarde, e capricha na água."
                            else -> "Dia perfeito pra correr ou caminhar em $cityName! Céu com $condition e vento agradável de $windSpeed km/h."
                        }
                    }
                    else -> {
                        "Aqui em $cityName a gente tá com $temp°C ($condition), com sensação de $feelsLike°C, umidade de $humidity% e vento de $windSpeed km/h. Tenha um dia maravilhoso!"
                    }
                }
            }
        }
    }
}
