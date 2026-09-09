package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.CurrentWeatherCard
import com.example.ui.model.AppLanguage
import com.example.ui.model.WeatherDetails
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleWeather = WeatherDetails(
      cityName = "São Paulo",
      stateCountry = "Brasil",
      latitude = -23.5505,
      longitude = -46.6333,
      currentTemp = 24.0,
      feelsLike = 25.0,
      minTemp = 18.0,
      maxTemp = 28.0,
      conditionCode = 1,
      isDay = true,
      humidity = 65,
      windSpeed = 12.0,
      windDirection = 180.0,
      uvIndex = 6.0,
      precipitationProbability = 10,
      sunrise = "06:14",
      sunset = "18:02",
      hourlyList = emptyList(),
      dailyList = emptyList(),
      aiInsight = "Clima perfeito para um passeio ao ar livre!"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        CurrentWeatherCard(
          weather = sampleWeather,
          isFavorite = true,
          selectedLanguage = AppLanguage.PT,
          onLanguageSelected = {},
          onToggleFavorite = {},
          onOpenSearch = {},
          onOpenAiChat = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
