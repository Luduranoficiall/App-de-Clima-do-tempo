package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CitySearchSheet
import com.example.ui.components.ClimaAiSheet
import com.example.ui.components.CurrentWeatherCard
import com.example.ui.components.DailyForecastList
import com.example.ui.components.HourlyForecastRow
import com.example.ui.components.WeatherBackground
import com.example.ui.components.WeatherMetricsGrid
import com.example.ui.model.AppLanguage
import com.example.ui.util.WeatherLocalization

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedCities by viewModel.savedCities.collectAsState()
    val lang = uiState.selectedLanguage

    var showSearchSheet by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val weather = uiState.weatherData
    val weatherCode = weather?.conditionCode ?: 0
    val isDay = weather?.isDay ?: true

    WeatherBackground(
        weatherCode = weatherCode,
        isDay = isDay
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAiSheet = true },
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Clima AI",
                            tint = Color(0xFFFFD54F)
                        )
                    },
                    text = {
                        Text(
                            text = "Clima AI",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                        .testTag("fab_clima_ai")
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading && weather == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                val loadingText = when (lang) {
                                    AppLanguage.ES -> "Cargando el pronóstico del clima…"
                                    AppLanguage.EN -> "Loading weather forecast…"
                                    AppLanguage.PT -> "Carregando previsão do tempo…"
                                }
                                Text(
                                    text = loadingText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    uiState.error != null && weather == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val errTitle = when (lang) {
                                        AppLanguage.ES -> "¡Ups!"
                                        AppLanguage.EN -> "Oops!"
                                        AppLanguage.PT -> "Ops!"
                                    }
                                    val retryText = when (lang) {
                                        AppLanguage.ES -> "Reintentar"
                                        AppLanguage.EN -> "Try Again"
                                        AppLanguage.PT -> "Tentar Novamente"
                                    }
                                    Text(
                                        text = errTitle,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.error ?: "",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(alpha = 0.8f),
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { viewModel.fetchDeviceLocation() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text(retryText)
                                    }
                                }
                            }
                        }
                    }

                    weather != null -> {
                        val isFavorite = savedCities.any { it.name.equals(weather.cityName, ignoreCase = true) }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .statusBarsPadding()
                                .padding(bottom = 88.dp)
                        ) {
                            // Top Refresh Indicator bar if refreshing
                            AnimatedVisibility(
                                visible = uiState.isRefreshing,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }

                            // Current Hero Weather Card with Language Switcher
                            CurrentWeatherCard(
                                weather = weather,
                                isFavorite = isFavorite,
                                selectedLanguage = lang,
                                onLanguageSelected = { viewModel.setLanguage(it) },
                                onToggleFavorite = { viewModel.toggleFavoriteCurrentCity() },
                                onOpenSearch = { showSearchSheet = true },
                                onOpenAiChat = { showAiSheet = true }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Hourly Carousel
                            HourlyForecastRow(
                                hourlyList = weather.hourlyList,
                                selectedLanguage = lang
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // 7-Day Forecast
                            DailyForecastList(
                                dailyList = weather.dailyList,
                                selectedLanguage = lang
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Atmospheric & Meteorological Metrics
                            WeatherMetricsGrid(
                                weather = weather,
                                selectedLanguage = lang
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // City Search Bottom Sheet
        if (showSearchSheet) {
            CitySearchSheet(
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                savedCities = savedCities,
                selectedLanguage = lang,
                onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onSelectCityResult = { viewModel.selectCitySearchResult(it) },
                onSelectSavedCity = { viewModel.selectSavedCity(it) },
                onRemoveSavedCity = { viewModel.removeSavedCity(it) },
                onUseCurrentLocation = { viewModel.fetchDeviceLocation() },
                onDismiss = { showSearchSheet = false }
            )
        }

        // Clima AI Assistant Bottom Sheet
        if (showAiSheet && weather != null) {
            ClimaAiSheet(
                cityName = weather.cityName,
                messages = uiState.chatMessages,
                isLoading = uiState.isAiLoading,
                selectedLanguage = lang,
                onSendMessage = { viewModel.sendAiMessage(it) },
                onDismiss = { showAiSheet = false }
            )
        }
    }
}
