package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.assistant.LiliAction
import com.example.assistant.LiliAssistantManager
import com.example.data.model.AppItem
import com.example.data.model.ClockType
import com.example.data.model.ThemeType
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lili Launcher", appName)
  }

  @Test
  fun `test Lili Assistant recognizes theme change command`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val assistant = LiliAssistantManager(context)
    val weather = WeatherInfo(
      temperature = 25,
      condition = "Limpo",
      highTemp = 28,
      lowTemp = 20,
      city = "São Paulo",
      iconEmoji = "☀️",
      humidity = 60
    )

    val response = assistant.processCommand("Lili mudar tema para Cyber", emptyList(), weather)
    assertTrue(response.action is LiliAction.ChangeTheme)
    assertEquals(ThemeType.CYBER_MATRIX, (response.action as LiliAction.ChangeTheme).theme)
  }

  @Test
  fun `test Lili Assistant recognizes clock change command`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val assistant = LiliAssistantManager(context)
    val weather = WeatherInfo(
      temperature = 25,
      condition = "Limpo",
      highTemp = 28,
      lowTemp = 20,
      city = "São Paulo",
      iconEmoji = "☀️",
      humidity = 60
    )

    val response = assistant.processCommand("Lili alterar relógio para analógico", emptyList(), weather)
    assertTrue(response.action is LiliAction.ChangeClock)
    assertEquals(ClockType.ANALOG_DIAL, (response.action as LiliAction.ChangeClock).clock)
  }
}
