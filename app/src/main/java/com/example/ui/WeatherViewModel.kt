package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeocodingResultDto
import com.example.data.db.SavedCityEntity
import com.example.data.db.WeatherDatabase
import com.example.data.location.LocationHelper
import com.example.data.repository.WeatherRepository
import com.example.ui.model.AppLanguage
import com.example.ui.model.ChatMessage
import com.example.ui.model.WeatherUiState
import com.example.ui.util.WeatherLocalization
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel principal responsável por gerenciar o estado da interface do aplicativo Clima Tempo.
 *
 * Implementa a arquitetura MVVM (Model-View-ViewModel), orquestrando:
 * - Obtenção de previsões meteorológicas em tempo real via [WeatherRepository].
 * - Persistência e gerenciamento de cidades salvas/favoritas no banco de dados local Room.
 * - Integração com o assistente inteligente [GeminiService].
 * - Suporte nativo a múltiplos idiomas ([AppLanguage]).
 * - Resolução de localização geográfica pelo dispositivo via [LocationHelper].
 *
 * @param application Instância da aplicação para injeção de contexto no banco de dados e serviços do sistema.
 */
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WeatherDatabase.getDatabase(application)
    private val repository = WeatherRepository(savedCityDao = db.savedCityDao())
    private val locationHelper = LocationHelper(application)

    /** Idioma padrão detectado a partir das preferências do dispositivo do usuário */
    private val initialLang: AppLanguage = AppLanguage.fromCode(Locale.getDefault().language)

    /** Estado interno mutável da UI, emitido como fluxo unidirecional para a interface */
    private val _uiState = MutableStateFlow(
        WeatherUiState(
            selectedLanguage = initialLang,
            chatMessages = listOf(
                ChatMessage(
                    text = WeatherLocalization.getAiInitialWelcome(initialLang),
                    isUser = false
                )
            )
        )
    )

    /** Fluxo público e imutável observado pela camada de apresentação Jetpack Compose */
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /** Lista reativa de cidades salvas no Room Database */
    val savedCities: StateFlow<List<SavedCityEntity>> = repository.savedCities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Job para controle de debounce na busca de cidades por texto */
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            // Inicializa cidades padrão caso o banco local esteja vazio
            repository.initDefaultCitiesIfEmpty()
            // Carrega dados iniciais de demonstração (São Paulo) ou última coordenada
            loadWeatherForCoordinates(-23.5505, -46.6333, "São Paulo", "Brasil")
        }
    }

    /**
     * Altera o idioma ativo do aplicativo e atualiza todas as previsões e mensagens do assistente.
     *
     * @param lang O novo idioma selecionado ([AppLanguage.PT], [AppLanguage.ES] ou [AppLanguage.EN]).
     */
    fun setLanguage(lang: AppLanguage) {
        if (_uiState.value.selectedLanguage == lang) return
        _uiState.update {
            it.copy(
                selectedLanguage = lang,
                chatMessages = listOf(
                    ChatMessage(
                        text = WeatherLocalization.getAiInitialWelcome(lang),
                        isUser = false
                    )
                )
            )
        }
        refreshCurrentWeather()
    }

    /**
     * Trata o resultado da requisição de permissões de localização (GPS).
     *
     * @param isGranted True se o usuário concedeu permissão de localização precisa ou aproximada.
     */
    fun onLocationPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted) }
        if (isGranted) {
            fetchDeviceLocation()
        }
    }

    /**
     * Obtém as coordenadas geográficas atuais do usuário através do GPS e carrega o clima correspondente.
     */
    fun fetchDeviceLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                loadWeatherForCoordinates(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    cityName = loc.cityName,
                    stateCountry = loc.stateName ?: "Sua Localização"
                )
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Carrega a previsão completa do tempo para um par de coordenadas específico.
     *
     * @param latitude Coordenada de latitude.
     * @param longitude Coordenada de longitude.
     * @param cityName Nome de exibição da cidade.
     * @param stateCountry Estado e país formatados para exibição.
     */
    fun loadWeatherForCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String,
        stateCountry: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val lang = _uiState.value.selectedLanguage
            try {
                val details = repository.fetchWeather(latitude, longitude, cityName, stateCountry, lang)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        weatherData = details,
                        error = null,
                        isSearching = false,
                        searchQuery = "",
                        searchResults = emptyList()
                    )
                }
            } catch (e: Exception) {
                val errText = when (lang) {
                    AppLanguage.ES -> "No pudimos cargar los datos del clima. Revisá tu conexión a internet e intentá de nuevo."
                    AppLanguage.EN -> "Unable to load weather data. Please check your internet connection and try again."
                    AppLanguage.PT -> "Não foi possível carregar os dados climáticos. Verifique sua conexão e tente novamente."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = errText
                    )
                }
            }
        }
    }

    /**
     * Atualiza os dados meteorológicos da cidade atualmente selecionada (Pull-to-refresh).
     */
    fun refreshCurrentWeather() {
        val current = _uiState.value.weatherData ?: return
        val lang = _uiState.value.selectedLanguage
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                val updated = repository.fetchWeather(
                    current.latitude,
                    current.longitude,
                    current.cityName,
                    current.stateCountry,
                    lang
                )
                _uiState.update { it.copy(isRefreshing = false, weatherData = updated) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    /**
     * Notifica a alteração do texto de pesquisa de cidades, aplicando debounce de 350ms.
     *
     * @param query Texto digitado pelo usuário na barra de pesquisa.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        val lang = _uiState.value.selectedLanguage
        searchJob = viewModelScope.launch {
            delay(350)
            val results = repository.searchCities(query, lang)
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    /**
     * Seleciona uma cidade dos resultados de busca, atualiza o clima e salva no histórico recente.
     *
     * @param city DTO retornado pela API de geocodificação da Open-Meteo.
     */
    fun selectCitySearchResult(city: GeocodingResultDto) {
        val stateCountry = listOfNotNull(city.admin1, city.country).joinToString(", ")
        loadWeatherForCoordinates(
            latitude = city.latitude,
            longitude = city.longitude,
            cityName = city.name,
            stateCountry = stateCountry
        )
        viewModelScope.launch {
            repository.saveCity(
                SavedCityEntity(
                    name = city.name,
                    state = city.admin1,
                    country = city.country,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    isFavorite = false
                )
            )
        }
    }

    /**
     * Alterna o status de favorito da cidade ativa no momento, persistindo a alteração no Room.
     */
    fun toggleFavoriteCurrentCity() {
        val weather = _uiState.value.weatherData ?: return
        viewModelScope.launch {
            val existing = savedCities.value.find { it.name.equals(weather.cityName, ignoreCase = true) }
            if (existing != null) {
                repository.removeCity(existing)
            } else {
                repository.saveCity(
                    SavedCityEntity(
                        name = weather.cityName,
                        state = weather.stateCountry,
                        country = null,
                        latitude = weather.latitude,
                        longitude = weather.longitude,
                        isFavorite = true
                    )
                )
            }
        }
    }

    /**
     * Carrega o clima a partir de uma entidade de cidade salva previamente.
     *
     * @param city A entidade persistida no Room Database.
     */
    fun selectSavedCity(city: SavedCityEntity) {
        val stateCountry = listOfNotNull(city.state, city.country).joinToString(", ")
        loadWeatherForCoordinates(
            latitude = city.latitude,
            longitude = city.longitude,
            cityName = city.name,
            stateCountry = if (stateCountry.isNotEmpty()) stateCountry else "Brasil"
        )
    }

    /**
     * Remove uma cidade da lista de favoritadas/salvas.
     *
     * @param city A entidade a ser excluída.
     */
    fun removeSavedCity(city: SavedCityEntity) {
        viewModelScope.launch {
            repository.removeCity(city)
        }
    }

    /**
     * Envia uma mensagem em linguagem natural para o assistente Clima AI e atualiza o histórico do chat.
     *
     * @param userText Pergunta ou observação enviada pelo usuário.
     */
    fun sendAiMessage(userText: String) {
        if (userText.isBlank()) return
        val currentMsg = ChatMessage(text = userText, isUser = true)
        val historyList = _uiState.value.chatMessages.map { (if (it.isUser) "user" else "model") to it.text }
        val lang = _uiState.value.selectedLanguage

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + currentMsg,
                isAiLoading = true
            )
        }

        viewModelScope.launch {
            val weather = _uiState.value.weatherData
            val response = if (weather != null) {
                repository.askAi(weather, userText, historyList, lang)
            } else {
                when (lang) {
                    AppLanguage.ES -> "No pude obtener los datos del clima en este momento para responder con precisión."
                    AppLanguage.EN -> "Unable to fetch current weather data to answer accurately right now."
                    AppLanguage.PT -> "Não consegui obter os dados meteorológicos no momento para responder com precisão."
                }
            }

            val botMsg = ChatMessage(text = response, isUser = false)
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + botMsg,
                    isAiLoading = false
                )
            }
        }
    }
}
