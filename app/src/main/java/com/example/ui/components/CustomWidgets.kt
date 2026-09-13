package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.WeatherInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// 1. WIDGET QUADRADO DE FOTO / VÍDEO LOOP
// ==========================================
@Composable
fun PhotoVideoSquareWidget(
    customPhotoUri: String?,
    mode: String, // "LOOP_CYBER", "LOOP_NEBULA", "LOOP_AURORA", "LOOP_LOFI", "CUSTOM_PHOTO"
    onSelectPhoto: (Uri) -> Unit,
    onChangeMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfigDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(135.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070B14).copy(alpha = 0.85f))
            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable { showConfigDialog = true }
            .testTag("photo_video_square_widget"),
        contentAlignment = Alignment.Center
    ) {
        when {
            mode == "CUSTOM_PHOTO" && !customPhotoUri.isNullOrBlank() -> {
                AsyncImage(
                    model = customPhotoUri,
                    contentDescription = "Foto Personalizada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            mode == "LOOP_NEBULA" -> {
                CosmicNebulaVideoLoopCanvas()
            }
            mode == "LOOP_AURORA" -> {
                AuroraWavesVideoLoopCanvas()
            }
            mode == "LOOP_LOFI" -> {
                RetroSynthVideoLoopCanvas()
            }
            else -> {
                // Default: LOOP_CYBER Matrix Energy Loop
                CyberMatrixVideoLoopCanvas()
            }
        }

        // Sleek compact badge indicator at top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .padding(horizontal = 5.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (mode == "CUSTOM_PHOTO") Icons.Default.Image else Icons.Default.PlayCircle,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(11.dp)
            )
        }

        // Overlay hint on bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
                .padding(bottom = 6.dp, top = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (mode == "CUSTOM_PHOTO") "Foto Frame" else "Vídeo Loop",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showConfigDialog) {
        PhotoVideoWidgetConfigDialog(
            currentMode = mode,
            currentUri = customPhotoUri,
            onSelectPhoto = { uri ->
                onSelectPhoto(uri)
                onChangeMode("CUSTOM_PHOTO")
            },
            onChangeMode = { newMode ->
                onChangeMode(newMode)
            },
            onDismiss = { showConfigDialog = false }
        )
    }
}

// Dialog for configuring Photo / Looping Video Widget
@Composable
fun PhotoVideoWidgetConfigDialog(
    currentMode: String,
    currentUri: String?,
    onSelectPhoto: (Uri) -> Unit,
    onChangeMode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            onSelectPhoto(uri)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1220)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎬 Widget de Foto / Vídeo",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Photo Option
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escolher Foto da Galeria", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("OU SELECIONE UM VÍDEO ANIMADO EM LOOPING:", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Presets list
                val presets = listOf(
                    "LOOP_CYBER" to "⚡ Cyber Matrix Loop",
                    "LOOP_NEBULA" to "🌌 Cosmic Galaxy Loop",
                    "LOOP_AURORA" to "🌊 Aurora Waves Loop",
                    "LOOP_LOFI" to "🌇 Synthwave Sun Loop"
                )

                presets.forEach { (modeKey, title) ->
                    val isSelected = currentMode == modeKey
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                onChangeMode(modeKey)
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, color = if (isSelected) Color(0xFF00E5FF) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Canvas Video Loop Animations
@Composable
private fun CyberMatrixVideoLoopCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_loop")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "cyber_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(color = Color(0xFF050914))

        val cols = 7
        val colWidth = width / cols
        for (i in 0 until cols) {
            val yOffset = ((animProgress * height * 1.5f + i * 45f) % (height + 60f)) - 30f
            drawRoundRect(
                color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                topLeft = Offset(i * colWidth + colWidth * 0.25f, yOffset),
                size = Size(colWidth * 0.5f, 25f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 3f,
                center = Offset(i * colWidth + colWidth * 0.5f, yOffset + 25f)
            )
        }
    }
}

@Composable
private fun CosmicNebulaVideoLoopCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "nebula_loop")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "nebula_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f

        drawRect(color = Color(0xFF080616))

        val rad = (animProgress * 3.14159f / 180f)
        for (i in 0 until 12) {
            val angle = rad + i * (3.14159f * 2f / 12f)
            val r = 25f + sin(rad * 2f + i) * 15f
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r

            drawCircle(
                color = if (i % 2 == 0) Color(0xFF818CF8).copy(alpha = 0.6f) else Color(0xFFC084FC).copy(alpha = 0.5f),
                radius = 8f + sin(angle) * 4f,
                center = Offset(x, y)
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE879F9).copy(alpha = 0.8f), Color.Transparent),
                center = Offset(cx, cy),
                radius = 45f
            ),
            radius = 45f,
            center = Offset(cx, cy)
        )
    }
}

@Composable
private fun AuroraWavesVideoLoopCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_loop")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "aurora_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF022C22), Color(0xFF0F172A))
            )
        )

        val path = Path().apply {
            moveTo(0f, h * 0.4f + sin(animProgress * 3.14f) * 20f)
            cubicTo(
                w * 0.3f, h * 0.2f - sin(animProgress * 3.14f) * 25f,
                w * 0.7f, h * 0.7f + cos(animProgress * 3.14f) * 25f,
                w, h * 0.5f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF34D399).copy(alpha = 0.6f), Color(0xFF06B6D4).copy(alpha = 0.1f))
            )
        )
    }
}

@Composable
private fun RetroSynthVideoLoopCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "retro_loop")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "retro_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF2E1065), Color(0xFF030712))
            )
        )

        // Sun
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFACC15), Color(0xFFEC4899))
            ),
            radius = w * 0.22f,
            center = Offset(w / 2f, h * 0.45f)
        )

        // Grid lines
        val horizon = h * 0.65f
        for (i in 0..5) {
            val y = horizon + ((i + animProgress) * (h - horizon) / 5f)
            drawLine(
                color = Color(0xFFEC4899).copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.5f
            )
        }
    }
}

// ==========================================
// 2. MINI CALENDÁRIO & AGENDA WIDGET
// ==========================================
@Composable
fun MiniCalendarWidget(
    modifier: Modifier = Modifier
) {
    val date = Date()
    val dayNum = SimpleDateFormat("dd", Locale("pt", "BR")).format(date)
    val dayName = SimpleDateFormat("EEE", Locale("pt", "BR")).format(date).uppercase()
    val monthName = SimpleDateFormat("MMM", Locale("pt", "BR")).format(date).uppercase()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0A0F1D).copy(alpha = 0.8f))
            .border(0.75.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("mini_calendar_widget")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                        .border(0.75.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = monthName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                        Text(text = dayNum, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(text = "$dayName, $monthName $dayNum", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(text = "📅 Próximo: Reunião Lili IA • 15:00", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                }
            }

            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ==========================================
// 3. WIDGET DE INSIGHT / FRASE DA LILI IA
// ==========================================
@Composable
fun LiliQuoteWidget(
    modifier: Modifier = Modifier
) {
    val quotes = remember {
        listOf(
            "\"O futuro pertence àqueles que constroem com paixão e visão.\"",
            "\"Seu tempo é limitado, torne cada momento épico.\"",
            "\"A Lili IA está pronta para transformar seus comandos em realidade.\"",
            "\"Mantenha o foco, simplifique e conquiste o dia!\""
        )
    }
    var currentQuoteIndex by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(0.75.dp, Color(0xFFA855F7).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("lili_quote_widget")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFC084FC),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = quotes[currentQuoteIndex],
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
        }
    }
}

// ==========================================
// 4. PLAYER DE MÚSICA REFINADO E COMPACTO
// ==========================================
@Composable
fun MediaQuickWidget(
    isPlaying: Boolean = true,
    onTogglePlay: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTrackIndex by remember { mutableStateOf(0) }
    val tracks = listOf("Synthwave Dreams • Lili Beats", "Neon Highway • Cyber Wave", "Lofi Chill • Aurora Zen")

    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val animVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "equalizer_animation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0A0E18).copy(alpha = 0.82f))
            .border(0.75.dp, Color(0xFF38BDF8).copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("media_quick_widget")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Canvas(modifier = Modifier.size(18.dp)) {
                    val barWidth = 3f
                    val spacing = 2f
                    for (i in 0 until 4) {
                        val barHeight = if (isPlaying) {
                            (8f + sin((animVal + i * 0.3f) * 3.14f) * 10f).coerceIn(3f, 16f)
                        } else {
                            4f
                        }
                        drawRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(i * (barWidth + spacing), size.height - barHeight),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = tracks[currentTrackIndex % tracks.size],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = if (isPlaying) "Reproduzindo" else "Pausado",
                        fontSize = 9.sp,
                        color = if (isPlaying) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else tracks.size - 1
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Tocar",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { currentTrackIndex = (currentTrackIndex + 1) % tracks.size },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Próxima", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ==========================================
// 5. BLOCO DE NOTAS RÁPIDO COMPACTO
// ==========================================
@Composable
fun StickyNotesWidget(
    initialText: String,
    onSaveText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(initialText) { mutableStateOf(initialText) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF18181B).copy(alpha = 0.82f))
            .border(0.75.dp, Color(0xFFFACC15).copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("sticky_notes_widget")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📌 Notas Rápida",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE047)
                )
            }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onSaveText(it)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                maxLines = 2
            )
        }
    }
}

// ==========================================
// 6. ESTATÍSTICAS DO SISTEMA COMPACTAS
// ==========================================
@Composable
fun DeviceStatsWidget(
    batteryLevel: Int,
    freeRamMb: Long,
    totalRamMb: Long,
    freeStorageGb: Long,
    totalStorageGb: Long,
    modifier: Modifier = Modifier
) {
    val ramUsedPercent = ((totalRamMb - freeRamMb).toFloat() / totalRamMb.coerceAtLeast(1L)).coerceIn(0f, 1f)
    val storageUsedPercent = ((totalStorageGb - freeStorageGb).toFloat() / totalStorageGb.coerceAtLeast(1L)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0A0E1A).copy(alpha = 0.8f))
            .border(0.75.dp, Color(0xFF6366F1).copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("device_stats_widget")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Battery
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔋", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$batteryLevel%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (batteryLevel > 20) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }

            // RAM
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(text = "RAM ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                LinearProgressIndicator(
                    progress = { ramUsedPercent },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF818CF8),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // Storage
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "SSD ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                Text(
                    text = "${freeStorageGb}GB livre",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
            }
        }
    }
}

// ==========================================
// 7. ATALHOS RÁPIDOS SEM BOLHAS / REFINADOS
// ==========================================
@Composable
fun QuickTogglesWidget(
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0B101D).copy(alpha = 0.78f))
            .border(0.75.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("quick_toggles_widget")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickToggleItem(
                label = "Lanterna",
                isActive = isTorchOn,
                activeColor = Color(0xFFF59E0B),
                icon = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                onClick = onToggleTorch
            )

            QuickToggleItem(
                label = "Wi-Fi",
                isActive = true,
                activeColor = Color(0xFF00E5FF),
                icon = Icons.Default.Wifi,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (_: Exception) {}
                }
            )

            QuickToggleItem(
                label = "Bluetooth",
                isActive = true,
                activeColor = Color(0xFF3B82F6),
                icon = Icons.Default.Bluetooth,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (_: Exception) {}
                }
            )

            QuickToggleItem(
                label = "Som",
                isActive = true,
                activeColor = Color(0xFF10B981),
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (_: Exception) {}
                }
            )
        }
    }
}

@Composable
fun QuickToggleItem(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}
