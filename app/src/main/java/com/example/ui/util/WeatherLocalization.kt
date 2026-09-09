package com.example.ui.util

import com.example.ui.model.AppLanguage
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utilitário central de internacionalização e localização dinâmica para o aplicativo Clima Tempo.
 *
 * Fornece traduções dinâmicas e contextuais para:
 * - Rótulos de tempo e calendário ("Hoje", "Amanhã", dias da semana abreviados).
 * - Marcadores horários ("Agora", "Ahora", "Now").
 * - Métricas atmosféricas (Sensação térmica, umidade, vento, índice UV, nascer/pôr do sol).
 * - Rótulos da interface de busca de cidades e listas salvas.
 * - Mensagens de boas-vindas, sugestões rápidas e instruções do assistente Clima AI.
 */
object WeatherLocalization {

    /**
     * Retorna a representação textual de "Hoje" no idioma especificado.
     */
    fun getTodayLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Hoje"
        AppLanguage.ES -> "Hoy"
        AppLanguage.EN -> "Today"
    }

    /**
     * Retorna o rótulo amigável para um dia específico da previsão de 7 dias.
     *
     * @param index Posição do dia na lista (0 = hoje, 1 = amanhã, 2+ = dia da semana).
     * @param dateStr Data no formato ISO `yyyy-MM-dd`.
     * @param lang Idioma para formatação.
     */
    fun getDayLabel(index: Int, dateStr: String, lang: AppLanguage): String {
        if (index == 0) {
            return getTodayLabel(lang)
        }
        if (index == 1) {
            return when (lang) {
                AppLanguage.PT -> "Amanhã"
                AppLanguage.ES -> "Mañana"
                AppLanguage.EN -> "Tomorrow"
            }
        }
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdfInput.parse(dateStr)
            if (date != null) {
                val locale = when (lang) {
                    AppLanguage.PT -> Locale.forLanguageTag("pt-BR")
                    AppLanguage.ES -> Locale.forLanguageTag("es-ES")
                    AppLanguage.EN -> Locale.ENGLISH
                }
                val sdfDay = SimpleDateFormat("EEE", locale)
                sdfDay.format(date).replaceFirstChar { it.uppercase() }
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    /** Retorna o marcador da hora atual */
    fun getNowLabel(lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.PT -> "Agora"
            AppLanguage.ES -> "Ahora"
            AppLanguage.EN -> "Now"
        }
    }

    /** Rótulo da métrica de Sensação Térmica */
    fun getFeelsLikeLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "SENSAÇÃO"
        AppLanguage.ES -> "SENSACIÓN"
        AppLanguage.EN -> "FEELS LIKE"
    }

    /** Rótulo da métrica de Umidade Relativa */
    fun getHumidityLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "UMIDADE"
        AppLanguage.ES -> "HUMEDAD"
        AppLanguage.EN -> "HUMIDITY"
    }

    /** Rótulo da métrica de Vento */
    fun getWindLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "VENTO"
        AppLanguage.ES -> "VIENTO"
        AppLanguage.EN -> "WIND"
    }

    /** Rótulo do Índice de Radiação Ultravioleta */
    fun getUvLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "ÍNDICE UV"
        AppLanguage.ES -> "ÍNDICE UV"
        AppLanguage.EN -> "UV INDEX"
    }

    /** Rótulo do horário de Nascer do Sol */
    fun getSunriseLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "NASCER DO SOL"
        AppLanguage.ES -> "AMANECER"
        AppLanguage.EN -> "SUNRISE"
    }

    /** Rótulo do horário de Pôr do Sol */
    fun getSunsetLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "PÔR DO SOL"
        AppLanguage.ES -> "ATARDECER"
        AppLanguage.EN -> "SUNSET"
    }

    /** Título da seção de previsão horária */
    fun getHourlyHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "PREVISÃO HORÁRIA"
        AppLanguage.ES -> "PRONÓSTICO POR HORA"
        AppLanguage.EN -> "HOURLY FORECAST"
    }

    /** Título da seção de previsão para 7 dias */
    fun getDailyHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "PREVISÃO PARA 7 DIAS"
        AppLanguage.ES -> "PRONÓSTICO PARA 7 DÍAS"
        AppLanguage.EN -> "7-DAY FORECAST"
    }

    /** Título do modal de pesquisa de cidades */
    fun getSearchCityTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Buscar Cidade"
        AppLanguage.ES -> "Buscar Ciudad"
        AppLanguage.EN -> "Search City"
    }

    /** Texto de orientação/placeholder do campo de busca */
    fun getSearchCityPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Ex: São Paulo, Rio, Lisboa, Madrid…"
        AppLanguage.ES -> "Ej: Madrid, Buenos Aires, Bogotá, Lima…"
        AppLanguage.EN -> "E.g.: New York, London, Madrid, São Paulo…"
    }

    /** Rótulo do botão para obter a localização atual via GPS */
    fun getUseGpsLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Usar Minha Localização Atual (GPS)"
        AppLanguage.ES -> "Usar Mi Ubicación Actual (GPS)"
        AppLanguage.EN -> "Use My Current Location (GPS)"
    }

    /** Cabeçalho da lista de cidades salvas/favoritas */
    fun getSavedCitiesHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "CIDADES SALVAS"
        AppLanguage.ES -> "CIUDADES GUARDADAS"
        AppLanguage.EN -> "SAVED CITIES"
    }

    /** Cabeçalho dos resultados de busca */
    fun getResultsHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "RESULTADOS"
        AppLanguage.ES -> "RESULTADOS"
        AppLanguage.EN -> "RESULTS"
    }

    /** Cabeçalho das sugestões rápidas de cidades */
    fun getQuickSuggestionsHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "SUGESTÕES RÁPIDAS"
        AppLanguage.ES -> "SUGERENCIAS RÁPIDAS"
        AppLanguage.EN -> "QUICK SUGGESTIONS"
    }

    /** Mensagem quando nenhuma cidade for encontrada */
    fun getNoCitiesFound(query: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Nenhuma cidade encontrada para \"$query\""
        AppLanguage.ES -> "No se encontraron ciudades para \"$query\""
        AppLanguage.EN -> "No cities found for \"$query\""
    }

    /** Instrução inicial exibida na barra de busca */
    fun getSearchPrompt(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Digite o nome de uma cidade para ver a previsão do tempo ao vivo."
        AppLanguage.ES -> "Escribe el nombre de una ciudad para ver el pronóstico del clima en vivo."
        AppLanguage.EN -> "Type a city name to see the live weather forecast."
    }

    /** Título exibido no cabeçalho do assistente Clima AI */
    fun getAiSheetTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Clima AI • Assistente"
        AppLanguage.ES -> "Clima AI • Asistente"
        AppLanguage.EN -> "Clima AI • Assistant"
    }

    /** Subtítulo com o nome da cidade analisada pelo assistente */
    fun getAiSheetSubtitle(cityName: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Respondendo sobre $cityName"
        AppLanguage.ES -> "Respondiendo sobre $cityName"
        AppLanguage.EN -> "Answering for $cityName"
    }

    /** Mensagem de abertura amigável do assistente */
    fun getAiInitialWelcome(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Olá! Sou seu assistente de clima pessoal. Como posso te ajudar hoje? Posso te sugerir que roupas usar, alertar sobre chuva ou te ajudar a planejar o seu dia com base na previsão atual."
        AppLanguage.ES -> "¡Hola! Soy tu asistente de clima personal. ¿En qué te puedo ayudar hoy? Te sugiero ropa adecuada, advertencias de lluvia o ayuda para planificar tu día con el pronóstico actual."
        AppLanguage.EN -> "Hello! I'm your personal weather assistant. How can I help you today? I can suggest what to wear, warn about rain, or help plan your activities based on current conditions."
    }

    /** Indicador de carregamento e processamento de pensamento do Clima AI */
    fun getAiThinkingMessage(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Clima AI está analisando o tempo…"
        AppLanguage.ES -> "Clima AI está analizando el tiempo…"
        AppLanguage.EN -> "Clima AI is analyzing the weather…"
    }

    /** Placeholder do campo de texto de conversa com a IA */
    fun getAiInputPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.PT -> "Pergunte algo sobre o tempo…"
        AppLanguage.ES -> "Pregunta algo sobre el clima…"
        AppLanguage.EN -> "Ask something about the weather…"
    }

    /** Perguntas rápidas pré-configuradas para os chips de sugestão do assistente */
    fun getQuickQuestions(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.PT -> listOf(
            "O que vestir hoje?",
            "Vai chover mais tarde?",
            "É bom para correr ao ar livre?",
            "Dicas para o clima atual"
        )
        AppLanguage.ES -> listOf(
            "¿Qué ropa usar hoy?",
            "¿Va a llover más tarde?",
            "¿Es bueno para correr afuera?",
            "Consejos para el clima de hoy"
        )
        AppLanguage.EN -> listOf(
            "What should I wear today?",
            "Will it rain later?",
            "Good for outdoor running?",
            "Tips for today's weather"
        )
    }

    /** Retorna a categoria descritiva do Índice UV conforme diretrizes da OMS */
    fun getUvCategory(uvIndex: Double, lang: AppLanguage): String {
        return when {
            uvIndex <= 2 -> when (lang) {
                AppLanguage.PT -> "Baixo"
                AppLanguage.ES -> "Bajo"
                AppLanguage.EN -> "Low"
            }
            uvIndex <= 5 -> when (lang) {
                AppLanguage.PT -> "Moderado"
                AppLanguage.ES -> "Moderado"
                AppLanguage.EN -> "Moderate"
            }
            uvIndex <= 7 -> when (lang) {
                AppLanguage.PT -> "Alto"
                AppLanguage.ES -> "Alto"
                AppLanguage.EN -> "High"
            }
            uvIndex <= 10 -> when (lang) {
                AppLanguage.PT -> "Muito Alto"
                AppLanguage.ES -> "Muy Alto"
                AppLanguage.EN -> "Very High"
            }
            else -> when (lang) {
                AppLanguage.PT -> "Extremo"
                AppLanguage.ES -> "Extremo"
                AppLanguage.EN -> "Extreme"
            }
        }
    }

    /** Retorna a descrição qualitativa do nível de umidade relativa */
    fun getHumiditySubtitle(humidity: Int, lang: AppLanguage): String {
        return when {
            humidity > 70 -> when (lang) {
                AppLanguage.PT -> "Umidade elevada"
                AppLanguage.ES -> "Humedad elevada"
                AppLanguage.EN -> "High humidity"
            }
            humidity < 30 -> when (lang) {
                AppLanguage.PT -> "Ar bastante seco"
                AppLanguage.ES -> "Aire bastante seco"
                AppLanguage.EN -> "Very dry air"
            }
            else -> when (lang) {
                AppLanguage.PT -> "Nível confortável"
                AppLanguage.ES -> "Nivel confortable"
                AppLanguage.EN -> "Comfortable level"
            }
        }
    }

    /** Retorna o comparativo entre a sensação térmica e a temperatura real */
    fun getFeelsLikeSubtitle(feelsLike: Double, currentTemp: Double, lang: AppLanguage): String {
        return if (feelsLike > currentTemp) {
            when (lang) {
                AppLanguage.PT -> "Mais quente que o real"
                AppLanguage.ES -> "Más cálido que el real"
                AppLanguage.EN -> "Warmer than actual"
            }
        } else {
            when (lang) {
                AppLanguage.PT -> "Similar à temperatura"
                AppLanguage.ES -> "Similar a la temperatura"
                AppLanguage.EN -> "Similar to actual"
            }
        }
    }

    /** Converte o ângulo em graus do vento para a sigla cardeal correspondente (N, NE, L/E, S, SO/SW, etc.) */
    fun getWindDirectionCardinal(degrees: Double, lang: AppLanguage): String {
        val index = (((degrees % 360) + 22.5) / 45.0).toInt() % 8
        return when (lang) {
            AppLanguage.PT -> arrayOf("N", "NE", "L", "SE", "S", "SO", "O", "NO")[index]
            AppLanguage.ES -> arrayOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")[index]
            AppLanguage.EN -> arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")[index]
        }
    }
}
