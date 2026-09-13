package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.AppItem
import com.example.data.model.ClockType
import com.example.data.model.FontType
import com.example.data.model.IconShape
import com.example.data.model.WeatherInfo
import com.example.ui.components.AppGrid
import com.example.ui.components.ClockWidget
import com.example.ui.components.InteractiveSphereAppGrid
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
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun launcher_clock_screenshot() {
    val sampleWeather = WeatherInfo(
      temperature = 24,
      condition = "Ensolarado",
      highTemp = 28,
      lowTemp = 18,
      city = "São Paulo",
      iconEmoji = "☀️",
      humidity = 58
    )

    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        ClockWidget(
          clockType = ClockType.CYBER_HUD,
          fontType = FontType.MONOSPACE,
          weatherInfo = sampleWeather
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun app_grid_inside_scrollable_container_does_not_crash() {
    val sampleApps = listOf(
      AppItem(packageName = "com.app.one", activityName = "com.app.one.MainActivity", label = "Phone", isPinned = true, sortOrder = 0),
      AppItem(packageName = "com.app.two", activityName = "com.app.two.MainActivity", label = "Messages", isPinned = true, sortOrder = 1),
      AppItem(packageName = "com.app.three", activityName = "com.app.three.MainActivity", label = "Camera", isPinned = true, sortOrder = 2),
      AppItem(packageName = "com.app.four", activityName = "com.app.four.MainActivity", label = "Settings", isPinned = true, sortOrder = 3)
    )

    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          AppGrid(
            apps = sampleApps,
            iconShape = IconShape.SQUIRCLE,
            iconSizeDp = 60,
            showLabels = true,
            fontType = FontType.SANS,
            columns = 4,
            isEditMode = false,
            getAppIcon = { _, _ -> null },
            onAppClick = {},
            onAppLongClick = {},
            onMoveLeft = {},
            onMoveRight = {},
            onRemoveFromHome = {},
            onEditLabel = {}
          )
        }
      }
    }
  }

  @Test
  fun interactive_sphere_app_grid_renders_without_crash() {
    val sampleApps = listOf(
      AppItem(packageName = "com.app.one", activityName = "com.app.one.MainActivity", label = "Phone", isPinned = true, sortOrder = 0),
      AppItem(packageName = "com.app.two", activityName = "com.app.two.MainActivity", label = "Messages", isPinned = true, sortOrder = 1),
      AppItem(packageName = "com.app.three", activityName = "com.app.three.MainActivity", label = "Camera", isPinned = true, sortOrder = 2),
      AppItem(packageName = "com.app.four", activityName = "com.app.four.MainActivity", label = "Settings", isPinned = true, sortOrder = 3),
      AppItem(packageName = "com.app.five", activityName = "com.app.five.MainActivity", label = "Gallery", isPinned = true, sortOrder = 4),
      AppItem(packageName = "com.app.six", activityName = "com.app.six.MainActivity", label = "Browser", isPinned = true, sortOrder = 5)
    )

    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          InteractiveSphereAppGrid(
            apps = sampleApps,
            iconShape = IconShape.SQUIRCLE,
            iconSizeDp = 56,
            showLabels = true,
            fontType = FontType.SANS,
            isEditMode = false,
            getAppIcon = { _, _ -> null },
            onAppClick = {},
            onAppLongClick = {},
            onMoveLeft = {},
            onMoveRight = {},
            onRemoveFromHome = {},
            onEditLabel = {}
          )
        }
      }
    }
  }
}
