package com.example.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LauncherPreferenceEntity
import com.example.data.model.AppGridLayout
import com.example.data.model.AppItem
import com.example.data.model.ClockType
import com.example.data.model.FontType
import com.example.data.model.IconShape
import com.example.data.model.PRESET_WALLPAPERS
import com.example.data.model.ThemeType
import com.example.data.model.WeatherInfo
import com.example.ui.components.AnimatedAppLayoutContainer
import com.example.ui.components.ClockWidget
import com.example.ui.components.DeviceStatsWidget
import com.example.ui.components.InteractiveThemeContainer
import com.example.ui.components.LiliQuoteWidget
import com.example.ui.components.MediaQuickWidget
import com.example.ui.components.MiniCalendarWidget
import com.example.ui.components.PhotoVideoSquareWidget
import com.example.ui.components.QuickTogglesWidget
import com.example.ui.components.StickyNotesWidget
import com.example.ui.components.TopStatusWeatherBar
import com.example.ui.components.getShapeForIcon
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenCustomizerScreen(
    initialPreferences: LauncherPreferenceEntity,
    weatherInfo: WeatherInfo,
    apps: List<AppItem> = emptyList(),
    onSavePreferences: (LauncherPreferenceEntity) -> Unit,
    onBack: () -> Unit
) {
    var preferences by remember { mutableStateOf(initialPreferences) }
    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    var isControlsExpanded by remember { mutableStateOf(true) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val updated = preferences.copy(customWallpaperUri = uri.toString())
            preferences = updated
            onSavePreferences(updated)
        }
    }

    val currentTheme = try {
        ThemeType.valueOf(preferences.theme)
    } catch (_: Exception) {
        ThemeType.CYBER_MATRIX
    }

    val currentClock = try {
        ClockType.valueOf(preferences.clockType)
    } catch (_: Exception) {
        ClockType.MINIMAL_DIGITAL
    }

    val currentClockFont = try {
        FontType.valueOf(preferences.clockFontType)
    } catch (_: Exception) {
        FontType.MONOSPACE
    }

    val currentFont = try {
        FontType.valueOf(preferences.fontType)
    } catch (_: Exception) {
        FontType.SANS
    }

    val currentShape = try {
        IconShape.valueOf(preferences.iconShape)
    } catch (_: Exception) {
        IconShape.SQUIRCLE
    }

    val currentAppLayout = try {
        AppGridLayout.valueOf(preferences.appGridLayout)
    } catch (_: Exception) {
        AppGridLayout.SPHERE_3D
    }

    val clockAlignmentEnum = when (preferences.clockAlignment) {
        "START" -> Alignment.CenterStart
        "END" -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    // Mock apps for simulator if list is empty
    val sampleApps = remember(apps) {
        if (apps.isNotEmpty()) apps else listOf(
            AppItem("com.google.android.youtube", "com.google.android.youtube.MainActivity", "YouTube", isPinned = true),
            AppItem("com.android.chrome", "com.google.android.apps.chrome.Main", "Chrome", isPinned = true),
            AppItem("com.spotify.music", "com.spotify.music.MainActivity", "Spotify", isPinned = true),
            AppItem("com.whatsapp", "com.whatsapp.Main", "WhatsApp", isPinned = true),
            AppItem("com.google.android.apps.maps", "com.google.android.maps.MapsActivity", "Maps", isPinned = true),
            AppItem("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail", "Gmail", isPinned = true),
            AppItem("com.google.android.apps.photos", "com.google.android.apps.photos.home.HomeActivity", "Fotos", isPinned = true),
            AppItem("com.android.calculator2", "com.android.calculator2.Calculator", "Calculadora", isPinned = true)
        )
    }

    InteractiveThemeContainer(
        currentTheme = currentTheme,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ==========================================
            // 1. SIMULAÇÃO EM TEMPO REAL EM TELA CHEIA (FULL SCREEN BACKGROUND)
            // ==========================================
            HomeScreenSimulatorContent(
                preferences = preferences,
                currentTheme = currentTheme,
                currentClock = currentClock,
                currentClockFont = currentClockFont,
                clockAlignmentEnum = clockAlignmentEnum,
                currentAppLayout = currentAppLayout,
                currentShape = currentShape,
                currentFont = currentFont,
                weatherInfo = weatherInfo,
                sampleApps = sampleApps,
                isMini = false,
                onUpdatePreferences = { updated ->
                    preferences = updated
                    onSavePreferences(updated)
                }
            )

            // ==========================================
            // 2. BARRA SUPERIOR SOBREPOSTA (TRANSLÚCIDA)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.0f))
                        )
                    )
                    .padding(top = 32.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão de Voltar + Título
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .testTag("simulator_back_button")
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Personalizar Tela",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Pré-visualização em tempo real",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Ações do Topo: Ocultar/Mostrar Controles + Botão Salvar
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { isControlsExpanded = !isControlsExpanded }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isControlsExpanded) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isControlsExpanded) "Ocultar" else "Opções",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onSavePreferences(preferences)
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Salvar", color = Color(0xFF070D1E), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }

            // ==========================================
            // 3. PAINEL DE OPÇÕES SIMPLIFICADO FLUTUANTE (OVERLAY INFERIOR TRANSLÚCIDO)
            // ==========================================
            AnimatedVisibility(
                visible = isControlsExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070B16).copy(alpha = 0.75f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    ) {
                        // Drag Indicator & Collapsible Pill
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                                .clickable { isControlsExpanded = false }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Customizer Categories (Scrollable Horizontal Tabs)
                        val categories = listOf("Temas", "Grade 3D", "Relógio", "Ícones", "Wallpaper", "Widgets")
                        ScrollableTabRow(
                            selectedTabIndex = selectedCategoryTab,
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF00E5FF),
                            edgePadding = 12.dp
                        ) {
                            categories.forEachIndexed { idx, label ->
                                Tab(
                                    selected = selectedCategoryTab == idx,
                                    onClick = { selectedCategoryTab = idx },
                                    text = {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedCategoryTab == idx) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedCategoryTab == idx) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                )
                            }
                        }

                        // Simplified Category Controls with Instant Live Update
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            when (selectedCategoryTab) {
                                0 -> { // Temas
                                    Text("Escolha o Tema Visual", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(ThemeType.entries.toTypedArray()) { theme ->
                                            val isSelected = theme == currentTheme
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f))
                                                    .clickable {
                                                        val updated = preferences.copy(theme = theme.name)
                                                        preferences = updated
                                                        onSavePreferences(updated)
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = theme.displayName,
                                                    color = if (isSelected) Color(0xFF0A0E1A) else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                1 -> { // Grade 3D
                                    Text("Modelo Animado da Tela", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(AppGridLayout.entries.toTypedArray()) { layout ->
                                            val isSelected = layout == currentAppLayout
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f))
                                                    .clickable {
                                                        val updated = preferences.copy(appGridLayout = layout.name)
                                                        preferences = updated
                                                        onSavePreferences(updated)
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = layout.displayName,
                                                    color = if (isSelected) Color(0xFF0A0E1A) else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Colunas da Grade", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(3, 4, 5).forEach { cols ->
                                                val isSelected = preferences.gridColumns == cols
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f))
                                                        .clickable {
                                                            val updated = preferences.copy(gridColumns = cols)
                                                            preferences = updated
                                                            onSavePreferences(updated)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("$cols cols", color = if (isSelected) Color(0xFF0A0E1A) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                2 -> { // Relógio
                                    Text("Estilo do Relógio", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(ClockType.entries.toTypedArray()) { clock ->
                                            val isSelected = clock == currentClock
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f))
                                                    .clickable {
                                                        val updated = preferences.copy(clockType = clock.name)
                                                        preferences = updated
                                                        onSavePreferences(updated)
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = clock.displayName,
                                                    color = if (isSelected) Color(0xFF0A0E1A) else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Tamanho", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                                        Text("${(preferences.clockScale * 100).roundToInt()}%", color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = preferences.clockScale,
                                        onValueChange = {
                                            val updated = preferences.copy(clockScale = it)
                                            preferences = updated
                                            onSavePreferences(updated)
                                        },
                                        valueRange = 0.6f..1.6f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Posição Vertical (Altura)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                                        Text("${preferences.clockVerticalOffset} dp", color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = preferences.clockVerticalOffset.toFloat(),
                                        onValueChange = {
                                            val updated = preferences.copy(clockVerticalOffset = it.roundToInt())
                                            preferences = updated
                                            onSavePreferences(updated)
                                        },
                                        valueRange = -30f..80f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                                    )
                                }

                                3 -> { // Ícones
                                    Text("Formato dos Ícones", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(IconShape.entries.toTypedArray()) { shape ->
                                            val isSelected = shape == currentShape
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f))
                                                    .clickable {
                                                        val updated = preferences.copy(iconShape = shape.name)
                                                        preferences = updated
                                                        onSavePreferences(updated)
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = shape.displayName,
                                                    color = if (isSelected) Color(0xFF0A0E1A) else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Tamanho dos Ícones", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                                        Text("${preferences.iconSize} dp", color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = preferences.iconSize.toFloat(),
                                        onValueChange = {
                                            val updated = preferences.copy(iconSize = it.roundToInt())
                                            preferences = updated
                                            onSavePreferences(updated)
                                        },
                                        valueRange = 36f..80f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Exibir Rótulos de Nome", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        Switch(
                                            checked = preferences.showAppLabels,
                                            onCheckedChange = {
                                                val updated = preferences.copy(showAppLabels = it)
                                                preferences = updated
                                                onSavePreferences(updated)
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF), checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f))
                                        )
                                    }
                                }

                                4 -> { // Wallpaper
                                    Text("Fundo de Tela Predefinido", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(PRESET_WALLPAPERS) { preset ->
                                            val isSelected = preset.id == preferences.presetWallpaperId && preferences.customWallpaperUri == null
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(Color(preset.primaryColorHex), Color(preset.secondaryColorHex))
                                                        )
                                                    )
                                                    .border(
                                                        2.dp,
                                                        if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        val updated = preferences.copy(presetWallpaperId = preset.id, customWallpaperUri = null)
                                                        preferences = updated
                                                        onSavePreferences(updated)
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(preset.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            photoPicker.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Escolher Foto da Galeria", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                5 -> { // Widgets
                                    Text("Widgets Visíveis na Tela", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    listOf(
                                        Triple("Widget Quadrado (Foto / Vídeo Loop)", preferences.showPhotoVideoWidget) { check: Boolean ->
                                            preferences.copy(showPhotoVideoWidget = check)
                                        },
                                        Triple("Mini Calendário & Agenda", preferences.showCalendarWidget) { check: Boolean ->
                                            preferences.copy(showCalendarWidget = check)
                                        },
                                        Triple("Frase Motivacional & Lili IA", preferences.showQuoteWidget) { check: Boolean ->
                                            preferences.copy(showQuoteWidget = check)
                                        },
                                        Triple("Monitor de Sistema (Bateria & RAM)", preferences.showMonitorWidget) { check: Boolean ->
                                            preferences.copy(showMonitorWidget = check)
                                        },
                                        Triple("Atalhos Rápidos (Lanterna & Som)", preferences.showTogglesWidget) { check: Boolean ->
                                            preferences.copy(showTogglesWidget = check)
                                        },
                                        Triple("Bloco de Notas Rápido", preferences.showNotesWidget) { check: Boolean ->
                                            preferences.copy(showNotesWidget = check)
                                        },
                                        Triple("Player de Mídia / Música", preferences.showMusicWidget) { check: Boolean ->
                                            preferences.copy(showMusicWidget = check)
                                        }
                                    ).forEach { (title, checked, updateFn) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(title, color = Color.White, fontSize = 12.sp)
                                            Switch(
                                                checked = checked,
                                                onCheckedChange = {
                                                    val updated = updateFn(it)
                                                    preferences = updated
                                                    onSavePreferences(updated)
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF), checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simulador fiel e proporcional da tela inicial do Lili Launcher.
 * Garante que 100% dos elementos (status bar, relógio, widgets, grade animada, dock e home bar)
 * sejam completamente visíveis e atualizados em tempo real.
 */
@Composable
fun HomeScreenSimulatorContent(
    preferences: LauncherPreferenceEntity,
    currentTheme: ThemeType,
    currentClock: ClockType,
    currentClockFont: FontType,
    clockAlignmentEnum: Alignment,
    currentAppLayout: AppGridLayout,
    currentShape: IconShape,
    currentFont: FontType,
    weatherInfo: WeatherInfo,
    sampleApps: List<AppItem>,
    isMini: Boolean,
    onUpdatePreferences: (LauncherPreferenceEntity) -> Unit
) {
    InteractiveThemeContainer(
        currentTheme = currentTheme,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isMini) {
                val refWidth = 360.dp
                val refHeight = 720.dp
                val calcScaleX = maxWidth / refWidth
                val calcScaleY = maxHeight / refHeight
                val previewScale = minOf(calcScaleX, calcScaleY) * 0.92f

                val scaledWidth = refWidth * previewScale
                val scaledHeight = refHeight * previewScale

                Box(
                    modifier = Modifier
                        .size(scaledWidth, scaledHeight)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .graphicsLayer {
                            scaleX = previewScale
                            scaleY = previewScale
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        }
                        .requiredSize(refWidth, refHeight)
                ) {
                    SimulatorLayoutContent(
                        preferences = preferences,
                        currentClock = currentClock,
                        currentClockFont = currentClockFont,
                        clockAlignmentEnum = clockAlignmentEnum,
                        currentAppLayout = currentAppLayout,
                        currentShape = currentShape,
                        currentFont = currentFont,
                        weatherInfo = weatherInfo,
                        sampleApps = sampleApps,
                        onUpdatePreferences = onUpdatePreferences
                    )
                }
            } else {
                // Modo Simulação em Tela Cheia: Ocupa 100% da tela do usuário perfeitamente centralizado
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SimulatorLayoutContent(
                        preferences = preferences,
                        currentClock = currentClock,
                        currentClockFont = currentClockFont,
                        clockAlignmentEnum = clockAlignmentEnum,
                        currentAppLayout = currentAppLayout,
                        currentShape = currentShape,
                        currentFont = currentFont,
                        weatherInfo = weatherInfo,
                        sampleApps = sampleApps,
                        onUpdatePreferences = onUpdatePreferences
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorLayoutContent(
    preferences: LauncherPreferenceEntity,
    currentClock: ClockType,
    currentClockFont: FontType,
    clockAlignmentEnum: Alignment,
    currentAppLayout: AppGridLayout,
    currentShape: IconShape,
    currentFont: FontType,
    weatherInfo: WeatherInfo,
    sampleApps: List<AppItem>,
    onUpdatePreferences: (LauncherPreferenceEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Status & Weather Bar
        TopStatusWeatherBar(
            weatherInfo = weatherInfo,
            onOpenLili = { /* preview only */ },
            onOpenSettings = { /* preview only */ },
            onWeatherClick = { /* preview only */ },
            includeStatusBarPadding = false
        )

        // 2. Interactive Drag & Live Position Clock Widget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dragSensitivity = 1.8f
                        val newX = (preferences.clockHorizontalOffset + dragAmount.x / dragSensitivity).roundToInt().coerceIn(-120, 120)
                        val newY = (preferences.clockVerticalOffset + dragAmount.y / dragSensitivity).roundToInt().coerceIn(-40, 90)
                        val updated = preferences.copy(clockHorizontalOffset = newX, clockVerticalOffset = newY)
                        onUpdatePreferences(updated)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            ClockWidget(
                clockType = currentClock,
                fontType = currentClockFont,
                weatherInfo = weatherInfo,
                alignment = clockAlignmentEnum,
                verticalOffsetDp = preferences.clockVerticalOffset,
                horizontalOffsetDp = preferences.clockHorizontalOffset,
                scale = preferences.clockScale
            )
        }

        // 3. Mini Active Widgets Preview (if enabled)
        if (preferences.showPhotoVideoWidget) {
            Box(modifier = Modifier.padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
                PhotoVideoSquareWidget(
                    customPhotoUri = preferences.photoWidgetUri,
                    mode = preferences.photoWidgetMode,
                    onSelectPhoto = { uri ->
                        val updated = preferences.copy(photoWidgetUri = uri.toString(), photoWidgetMode = "CUSTOM_PHOTO")
                        onUpdatePreferences(updated)
                    },
                    onChangeMode = { newMode ->
                        val updated = preferences.copy(photoWidgetMode = newMode)
                        onUpdatePreferences(updated)
                    }
                )
            }
        }
        if (preferences.showCalendarWidget) {
            MiniCalendarWidget()
        }
        if (preferences.showQuoteWidget) {
            LiliQuoteWidget()
        }
        if (preferences.showMonitorWidget) {
            DeviceStatsWidget(
                batteryLevel = 85,
                freeRamMb = 3400,
                totalRamMb = 8192,
                freeStorageGb = 64,
                totalStorageGb = 128
            )
        }
        if (preferences.showTogglesWidget) {
            QuickTogglesWidget(
                isTorchOn = false,
                onToggleTorch = {}
            )
        }
        if (preferences.showNotesWidget) {
            StickyNotesWidget(
                initialText = preferences.stickyNoteContent,
                onSaveText = { newText ->
                    val updated = preferences.copy(stickyNoteContent = newText)
                    onUpdatePreferences(updated)
                }
            )
        }
        if (preferences.showMusicWidget) {
            MediaQuickWidget(
                isPlaying = true,
                onTogglePlay = {}
            )
        }

        // 4. Animated 3D App Layout Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedAppLayoutContainer(
                apps = sampleApps,
                layout = currentAppLayout,
                iconShape = currentShape,
                iconSizeDp = preferences.iconSize,
                showLabels = preferences.showAppLabels,
                fontType = currentFont,
                columns = preferences.gridColumns,
                isEditMode = false,
                getAppIcon = { _, _ -> null },
                onAppClick = { /* preview only */ },
                onAppLongClick = { /* preview only */ },
                onMoveLeft = {},
                onMoveRight = {},
                onRemoveFromHome = {},
                onEditLabel = {}
            )
        }

        // 5. Bottom Dock with 4 essential apps
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp, start = 12.dp, end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Icons.Default.Phone to Color(0xFF10B981),
                    Icons.Default.Email to Color(0xFF3B82F6),
                    Icons.Default.Public to Color(0xFF00E5FF),
                    Icons.Default.CameraAlt to Color(0xFFF59E0B)
                ).forEach { (icon, color) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(getShapeForIcon(currentShape))
                            .background(color.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 6. Home Gesture Bar at bottom
        Box(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .size(width = 36.dp, height = 3.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
        )
    }
}


