package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherInfo

@Composable
fun TopStatusWeatherBar(
    weatherInfo: WeatherInfo,
    onOpenLili: () -> Unit,
    onOpenSettings: () -> Unit,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier,
    includeStatusBarPadding: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (includeStatusBarPadding) Modifier.statusBarsPadding() else Modifier)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left corner: Weather on top and AI Assistant button directly below it (cleanly below the phone status bar)
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Simplified Weather chip/text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onWeatherClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("top_corner_weather")
            ) {
                Text(
                    text = "${weatherInfo.iconEmoji} ${weatherInfo.temperature}°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // AI button: Pure icon button directly under the weather
            IconButton(
                onClick = onOpenLili,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("lili_assistant_quick_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Assistente IA",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Right side: Settings button placed neatly below status bar
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(40.dp)
                .testTag("launcher_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurações do Launcher",
                tint = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


