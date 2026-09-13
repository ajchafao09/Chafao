package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockType
import com.example.data.model.FontType
import com.example.data.model.IconShape
import com.example.data.model.ThemeType
import com.example.data.model.WeatherInfo
import com.example.ui.components.getFontFamily
import com.example.ui.components.getShapeForIcon

@Composable
fun LivePreviewCard(
    theme: ThemeType,
    clockType: ClockType,
    fontType: FontType,
    clockFontType: FontType,
    iconShape: IconShape,
    weatherInfo: WeatherInfo,
    showLabels: Boolean,
    modifier: Modifier = Modifier
) {
    val font = getFontFamily(fontType)
    val clockFont = getFontFamily(clockFontType)
    val shape = getShapeForIcon(iconShape)

    val backgroundBrush = when (theme) {
        ThemeType.CYBER_MATRIX -> Brush.verticalGradient(listOf(Color(0xFF0A0F1D), Color(0xFF003049)))
        ThemeType.COSMIC_NEBULA -> Brush.radialGradient(listOf(Color(0xFF2E1065), Color(0xFF0F0728)))
        ThemeType.ZEN_AURORA -> Brush.verticalGradient(listOf(Color(0xFF022C22), Color(0xFF0B132B)))
        ThemeType.RETRO_ARCADE -> Brush.verticalGradient(listOf(Color(0xFF3B0764), Color(0xFF1E0533)))
        ThemeType.BIO_FOREST -> Brush.verticalGradient(listOf(Color(0xFF052E16), Color(0xFF02160C)))
    }

    val accentColor = when (theme) {
        ThemeType.CYBER_MATRIX -> Color(0xFF00E5FF)
        ThemeType.COSMIC_NEBULA -> Color(0xFFC084FC)
        ThemeType.ZEN_AURORA -> Color(0xFF34D399)
        ThemeType.RETRO_ARCADE -> Color(0xFFFF007F)
        ThemeType.BIO_FOREST -> Color(0xFFA7F3D0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0A0E1A))
            .border(1.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRÉVIA EM TEMPO REAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = theme.displayName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simulated mini phone screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(backgroundBrush)
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mini status corner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "09:41", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "${weatherInfo.iconEmoji} ${weatherInfo.temperature}°C", fontSize = 10.sp, color = Color(0xFFFFD54F))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mini Clock
                when (clockType) {
                    ClockType.MINIMAL_DIGITAL -> {
                        Text(
                            text = "09:41",
                            fontFamily = clockFont,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(text = "Segunda-feira, 12 de Setembro", fontFamily = clockFont, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    ClockType.CYBER_HUD -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(text = "09:41 :35 // SYS_OK", fontFamily = clockFont, fontSize = 14.sp, color = accentColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    ClockType.ANALOG_DIAL -> {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.5.dp, accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🕒", fontSize = 22.sp)
                        }
                        Text(text = "09:41 AM", fontFamily = clockFont, fontSize = 11.sp, color = Color.White)
                    }
                    ClockType.RETRO_FLIP -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.Black).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(text = "09", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                            Text(text = ":", fontSize = 18.sp, color = Color.White)
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.Black).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(text = "41", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                        }
                    }
                    ClockType.TYPOGRAPHY -> {
                        Text(text = "NOVE HORAS", fontFamily = clockFont, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text(text = "E QUARENTA E UM", fontFamily = clockFont, fontSize = 12.sp, color = accentColor)
                    }
                    ClockType.GLASS_PILL -> {
                        Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text(text = "09:41 • 24°C ☀️", fontFamily = clockFont, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mini App Icons preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val sampleIcons = listOf(
                        Triple(Icons.Default.Phone, "Telefone", Color(0xFF10B981)),
                        Triple(Icons.Default.Email, "Mensagens", Color(0xFF3B82F6)),
                        Triple(Icons.Default.Public, "Navegador", Color(0xFFF59E0B)),
                        Triple(Icons.Default.CameraAlt, "Câmera", Color(0xFFEC4899))
                    )

                    sampleIcons.forEach { (icon, name, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(shape)
                                    .background(color.copy(alpha = 0.35f))
                                    .border(1.dp, color, shape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (showLabels) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = name,
                                    fontFamily = font,
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
