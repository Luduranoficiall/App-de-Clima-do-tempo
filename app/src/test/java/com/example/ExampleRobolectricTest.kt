package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.model.AppLanguage
import com.example.ui.model.WeatherType
import com.example.ui.util.WeatherLocalization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertNotNull(appName)
  }

  @Test
  fun `verify wmo weather codes map correctly in Portuguese`() {
    val sunnyPt = WeatherType.fromWmo(0, isDay = true, lang = AppLanguage.PT)
    assertEquals("Céu Limpo", sunnyPt.description)

    val rainPt = WeatherType.fromWmo(63, isDay = true, lang = AppLanguage.PT)
    assertEquals("Chuva Moderada", rainPt.description)
    assert(rainPt.isRainy)

    val stormPt = WeatherType.fromWmo(95, isDay = true, lang = AppLanguage.PT)
    assertEquals("Tempestade com Raios", stormPt.description)
  }

  @Test
  fun `verify multilingual translations in Spanish and English`() {
    val sunnyEs = WeatherType.fromWmo(0, isDay = true, lang = AppLanguage.ES)
    assertEquals("Cielo Despejado", sunnyEs.description)

    val sunnyEn = WeatherType.fromWmo(0, isDay = true, lang = AppLanguage.EN)
    assertEquals("Clear Sky", sunnyEn.description)

    val todayEs = WeatherLocalization.getTodayLabel(AppLanguage.ES)
    assertEquals("Hoy", todayEs)

    val todayPt = WeatherLocalization.getTodayLabel(AppLanguage.PT)
    assertEquals("Hoje", todayPt)

    val todayEn = WeatherLocalization.getTodayLabel(AppLanguage.EN)
    assertEquals("Today", todayEn)
  }
}
