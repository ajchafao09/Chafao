package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockType
import com.example.data.model.FontType
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun getFontFamily(fontType: FontType): FontFamily {
    return when (fontType) {
        FontType.SANS -> FontFamily.SansSerif
        FontType.MONOSPACE -> FontFamily.Monospace
        FontType.SERIF -> FontFamily.Serif
        FontType.CURSIVE -> FontFamily.Cursive
        FontType.ROUNDED -> FontFamily.Default
    }
}

@Composable
fun ClockWidget(
    clockType: ClockType,
    fontType: FontType,
    weatherInfo: WeatherInfo? = null,
    alignment: Alignment = Alignment.Center,
    verticalOffsetDp: Int = 0,
    horizontalOffsetDp: Int = 0,
    scale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val cal = Calendar.getInstance().apply { time = currentTime }
    val hours24 = cal.get(Calendar.HOUR_OF_DAY)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)

    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hours24, minutes)
    val dateStr = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("pt-BR")).format(currentTime).replaceFirstChar { it.uppercase() }

    val clockFont = getFontFamily(fontType)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .offset(x = horizontalOffsetDp.dp, y = verticalOffsetDp.dp)
                .scale(scale.coerceIn(0.6f, 1.8f)),
            contentAlignment = alignment
        ) {
            when (clockType) {
                ClockType.MINIMAL_DIGITAL -> MinimalDigitalClock(timeStr, dateStr, seconds, clockFont)
                ClockType.CYBER_HUD -> CyberHudClock(timeStr, dateStr, seconds, clockFont)
                ClockType.ANALOG_DIAL -> AnalogDialClock(hours24, minutes, seconds, dateStr, clockFont)
                ClockType.RETRO_FLIP -> RetroFlipClock(hours24, minutes, dateStr, clockFont)
                ClockType.TYPOGRAPHY -> TypographyTextClock(hours24, minutes, dateStr, clockFont)
                ClockType.GLASS_PILL -> GlassPillClock(timeStr, dateStr, clockFont)
            }
        }
    }
}

@Composable
fun MinimalDigitalClock(
    timeStr: String,
    dateStr: String,
    seconds: Int,
    fontFamily: FontFamily
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeStr,
                fontFamily = fontFamily,
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = String.format(Locale.ROOT, "%02d", seconds),
                fontFamily = fontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF00E5FF).copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Text(
            text = dateStr,
            fontFamily = fontFamily,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun CyberHudClock(
    timeStr: String,
    dateStr: String,
    seconds: Int,
    fontFamily: FontFamily
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF080D1A).copy(alpha = 0.8f))
            .border(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF00F0FF), Color(0xFFFF007F))), RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SYS_ONLINE // 0x7E",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF00F0FF).copy(alpha = 0.8f)
                )
                Text(
                    text = "ACTIVE // SYSTEM",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFFFF007F).copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeStr,
                    fontFamily = fontFamily,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00F0FF),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ":$seconds",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    color = Color(0xFFFF007F),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = dateStr.uppercase(),
                fontFamily = fontFamily,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AnalogDialClock(
    hours: Int,
    minutes: Int,
    seconds: Int,
    dateStr: String,
    fontFamily: FontFamily
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(150.dp)) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Dial background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E1B4B).copy(alpha = 0.7f), Color(0xFF0F172A).copy(alpha = 0.9f))
                    ),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF818CF8).copy(alpha = 0.5f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.5f)
                )

                // Hour tick marks
                for (i in 0 until 12) {
                    val angle = (i * 30f) * (PI / 180f).toFloat()
                    val startR = radius * 0.85f
                    val endR = radius * 0.95f
                    val start = Offset(center.x + sin(angle) * startR, center.y - cos(angle) * startR)
                    val end = Offset(center.x + sin(angle) * endR, center.y - cos(angle) * endR)
                    drawLine(
                        color = if (i % 3 == 0) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.4f),
                        start = start,
                        end = end,
                        strokeWidth = if (i % 3 == 0) 3.5f else 1.5f,
                        cap = StrokeCap.Round
                    )
                }

                // Hour Hand
                val hourAngle = ((hours % 12 + minutes / 60f) * 30f) * (PI / 180f).toFloat()
                val hourLen = radius * 0.52f
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(center.x + sin(hourAngle) * hourLen, center.y - cos(hourAngle) * hourLen),
                    strokeWidth = 4.5f,
                    cap = StrokeCap.Round
                )

                // Minute Hand
                val minAngle = ((minutes + seconds / 60f) * 6f) * (PI / 180f).toFloat()
                val minLen = radius * 0.75f
                drawLine(
                    color = Color(0xFF38BDF8),
                    start = center,
                    end = Offset(center.x + sin(minAngle) * minLen, center.y - cos(minAngle) * minLen),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Second Hand
                val secAngle = (seconds * 6f) * (PI / 180f).toFloat()
                val secLen = radius * 0.85f
                drawLine(
                    color = Color(0xFFF43F5E),
                    start = center,
                    end = Offset(center.x + sin(secAngle) * secLen, center.y - cos(secAngle) * secLen),
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )

                // Center pivot
                drawCircle(color = Color(0xFFF43F5E), radius = 5f, center = center)
                drawCircle(color = Color.White, radius = 2f, center = center)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dateStr,
            fontFamily = fontFamily,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun RetroFlipClock(
    hours: Int,
    minutes: Int,
    dateStr: String,
    fontFamily: FontFamily
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            FlipDigitCard(String.format(Locale.ROOT, "%02d", hours), fontFamily)
            Text(
                text = ":",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF80AB),
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            FlipDigitCard(String.format(Locale.ROOT, "%02d", minutes), fontFamily)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dateStr,
            fontFamily = fontFamily,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun FlipDigitCard(text: String, fontFamily: FontFamily) {
    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 70.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E28))
            .border(1.dp, Color(0xFF3F3F50), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Split line in middle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF0F0F16))
        )
        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFD54F),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TypographyTextClock(
    hours: Int,
    minutes: Int,
    dateStr: String,
    fontFamily: FontFamily
) {
    val hourWords = listOf(
        "MEIA-NOITE", "UMA HORA", "DUAS HORAS", "TRÊS HORAS", "QUATRO HORAS",
        "CINCO HORAS", "SEIS HORAS", "SETE HORAS", "OITO HORAS", "NOVE HORAS",
        "DEZ HORAS", "ONZE HORAS", "MEIO-DIA", "TREZE HORAS", "QUATORZE HORAS",
        "QUINZE HORAS", "DEZESSEIS HORAS", "DEZESSETE HORAS", "DEZOITO HORAS",
        "DEZENOVE HORAS", "VINTE HORAS", "VINTE E UMA HORAS", "VINTE E DUAS HORAS", "VINTE E TRÊS HORAS"
    )
    val hourText = hourWords.getOrElse(hours) { "$hours HORAS" }
    val minText = when (minutes) {
        0 -> "EM PONTO"
        15 -> "E QUINZE"
        30 -> "E MEIA"
        45 -> "E QUARENTA E CINCO"
        else -> "E $minutes MINUTOS"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = hourText,
            fontFamily = fontFamily,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = minText,
            fontFamily = fontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateStr,
            fontFamily = fontFamily,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun GlassPillClock(
    timeStr: String,
    dateStr: String,
    fontFamily: FontFamily
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = timeStr,
                fontFamily = fontFamily,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color.White.copy(alpha = 0.3f))
            )
            Column {
                Text(
                    text = dateStr,
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
