package com.example.ui.settings

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ui.components.drawableToBitmap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BackupEntity
import com.example.data.local.LauncherPreferenceEntity
import com.example.data.model.AppGridLayout
import com.example.data.model.AppItem
import com.example.data.model.ClockType
import com.example.data.model.FontType
import com.example.data.model.IconShape
import com.example.data.model.PRESET_THIRD_PARTY_THEMES
import com.example.data.model.PRESET_WALLPAPERS
import com.example.data.model.ThemeType
import com.example.data.model.WeatherInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentPreferences: LauncherPreferenceEntity,
    weatherInfo: WeatherInfo,
    backups: List<BackupEntity>,
    apps: List<AppItem> = emptyList(),
    hiddenApps: List<AppItem> = emptyList(),
    getAppIcon: (String, String) -> Drawable? = { _, _ -> null },
    onUpdatePreferences: (LauncherPreferenceEntity) -> Unit,
    onCreateBackup: (String) -> Unit,
    onRestoreBackup: (BackupEntity) -> Unit,
    onDeleteBackup: (Long) -> Unit,
    onUnhideApp: (AppItem) -> Unit = {},
    onUnhideAllApps: () -> Unit = {},
    onRequestSetDefaultHome: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showHomeScreenCustomizer by remember { mutableStateOf(false) }

    if (showHomeScreenCustomizer) {
        HomeScreenCustomizerScreen(
            initialPreferences = currentPreferences,
            weatherInfo = weatherInfo,
            apps = apps,
            onSavePreferences = onUpdatePreferences,
            onBack = { showHomeScreenCustomizer = false }
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Apps Ocultos", "Backup & Nuvem", "Terceiros", "Lili IA")

    // Image Picker for Gallery Wallpaper
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdatePreferences(currentPreferences.copy(customWallpaperUri = uri.toString()))
        }
    }

    val currentAppLayout = try {
        AppGridLayout.valueOf(currentPreferences.appGridLayout)
    } catch (_: Exception) {
        AppGridLayout.SPHERE_3D
    }

    val currentTheme = try {
        ThemeType.valueOf(currentPreferences.theme)
    } catch (_: Exception) {
        ThemeType.CYBER_MATRIX
    }

    val currentClock = try {
        ClockType.valueOf(currentPreferences.clockType)
    } catch (_: Exception) {
        ClockType.MINIMAL_DIGITAL
    }

    val currentFont = try {
        FontType.valueOf(currentPreferences.fontType)
    } catch (_: Exception) {
        FontType.SANS
    }

    val currentClockFont = try {
        FontType.valueOf(currentPreferences.clockFontType)
    } catch (_: Exception) {
        FontType.MONOSPACE
    }

    val currentShape = try {
        IconShape.valueOf(currentPreferences.iconShape)
    } catch (_: Exception) {
        IconShape.SQUIRCLE
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configurações do Lili Launcher",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090D16)
                )
            )
        },
        containerColor = Color(0xFF060911)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // CONFIGURAÇÕES DE TELA INICIAL (Single dedicated option card)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF2E1065)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFFA855F7), Color(0xFFEC4899))
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { showHomeScreenCustomizer = true }
                        .padding(16.dp)
                        .testTag("open_home_screen_customizer_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "CONFIGURAÇÕES DE TELA INICIAL",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Simulador interativo ao vivo: ajuste relógio, posição, tamanho, temas, grade e widgets",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF00E5FF))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "Abrir",
                                color = Color(0xFF070D1E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Tabs for organized navigation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0D1220),
                contentColor = Color(0xFF00E5FF),
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Launcher Principal prompt
                item {
                    DefaultLauncherBanner(onRequestSetDefaultHome = onRequestSetDefaultHome)
                }

                when (selectedTab) {
                    0 -> { // Apps Ocultos
                        item {
                            SectionHeader(
                                title = "Aplicativos Ocultos",
                                subtitle = "Gerencie e restaure os aplicativos escondidos do launcher"
                            )
                        }

                        if (hiddenApps.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Text(
                                        text = "Nenhum aplicativo oculto",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "Para ocultar um aplicativo da tela inicial e da gaveta de apps, pressione e segure no ícone dele e escolha 'Ocultar Aplicativo'.",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.65f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        } else {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${hiddenApps.size} aplicativo(s) oculto(s)",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )

                                    Button(
                                        onClick = onUnhideAllApps,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Exibir Todos", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }

                            items(hiddenApps, key = { it.uniqueKey }) { app ->
                                val iconDrawable = remember(app.uniqueKey) { getAppIcon(app.packageName, app.activityName) }
                                val bitmap = remember(iconDrawable) { drawableToBitmap(iconDrawable) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = app.displayLabel,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        } else {
                                            Text(
                                                text = app.displayLabel.take(1).uppercase(),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.displayLabel,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = app.packageName,
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }

                                    Button(
                                        onClick = { onUnhideApp(app) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Exibir app",
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Exibir", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Backup & Nuvem
                        item {
                            SectionHeader(
                                title = "Backup & Sincronização em Nuvem",
                                subtitle = "Salve seu layout favorito na nuvem e sincronize em múltiplos dispositivos"
                            )
                        }
                        item {
                            var backupName by remember { mutableStateOf("Meu Layout Favorito") }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = backupName,
                                    onValueChange = { backupName = it },
                                    label = { Text("Nome do Backup") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00E5FF),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (backupName.isNotBlank()) {
                                            onCreateBackup(backupName)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = "Salvar na Nuvem", tint = Color(0xFF0A0E1A))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Salvar Layout na Nuvem", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Backups Salvos (${backups.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        if (backups.isEmpty()) {
                            item {
                                Text("Nenhum backup criado ainda.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                            }
                        } else {
                            items(backups) { backup ->
                                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(backup.timestamp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.07f))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = backup.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Text(text = "${backup.deviceModel} • $date", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    }
                                    Row {
                                        IconButton(onClick = { onRestoreBackup(backup) }) {
                                            Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = Color(0xFF34D399))
                                        }
                                        IconButton(onClick = { onDeleteBackup(backup.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Terceiros
                        item {
                            SectionHeader(title = "Integração Profunda com Temas de Terceiros", subtitle = "Pacotes visuais comunitários estilizados para Lili Launcher")
                        }
                        items(PRESET_THIRD_PARTY_THEMES) { tpTheme ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF131828))
                                    .border(1.dp, Color(tpTheme.accentColorHex).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        onUpdatePreferences(
                                            currentPreferences.copy(
                                                iconShape = tpTheme.shape.name,
                                                fontType = tpTheme.font.name
                                            )
                                        )
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = tpTheme.name, fontWeight = FontWeight.Bold, color = Color(tpTheme.accentColorHex), fontSize = 15.sp)
                                        Text(text = "Formato: ${tpTheme.shape.displayName} • Fonte: ${tpTheme.font.displayName}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = {
                                            onUpdatePreferences(
                                                currentPreferences.copy(
                                                    iconShape = tpTheme.shape.name,
                                                    fontType = tpTheme.font.name
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(tpTheme.accentColorHex))
                                    ) {
                                        Text("Aplicar", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Lili IA
                        item {
                            SectionHeader(title = "Assistente Lili IA", subtitle = "Inteligência artificial integrada com total acesso ao celular")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF1E1135))
                                    .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text("O que a Lili pode fazer por você:", fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA), fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• Chamar 'Lili' ou 'Ei Lili' para ativar instantaneamente\n• Abrir qualquer aplicativo instalado por comando de voz ou texto\n• Ligar e desligar a lanterna do aparelho\n• Mudar temas animados e formatos de relógio\n• Informar dados de bateria, memória RAM e armazenamento\n• Consultar clima ao vivo baseado na sua cidade\n• Conversação avançada com Gemini 3.5 Flash", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 20.sp)
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun DefaultLauncherBanner(onRequestSetDefaultHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
                )
            )
            .padding(14.dp)
            .testTag("default_launcher_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Definir como Launcher Principal",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "Torne o Lili seu iniciador padrão para uma experiência completa",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestSetDefaultHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "Definir", color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun ThemeOptionCard(
    theme: ThemeType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.5.dp,
                if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("theme_card_${theme.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.displayName,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                    fontSize = 15.sp
                )
                Text(
                    text = theme.description,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionado",
                    tint = Color(0xFF00E5FF)
                )
            }
        }
    }
}

@Composable
fun ClockOptionCard(
    clock: ClockType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.5.dp,
                if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("clock_card_${clock.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = clock.displayName,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                fontSize = 14.sp
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionado",
                    tint = Color(0xFF00E5FF)
                )
            }
        }
    }
}

@Composable
fun FontChip(
    font: FontType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = font.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF0A0E1A) else Color.White
        )
    }
}

@Composable
fun ShapeChip(
    shape: IconShape,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = shape.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF0A0E1A) else Color.White
        )
    }
}

@Composable
fun WidgetToggleRow(
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = desc, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00E5FF),
                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun AppLayoutOptionCard(
    layout: AppGridLayout,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.5.dp,
                if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("app_layout_card_${layout.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (layout) {
                    AppGridLayout.SPHERE_3D -> Icons.Default.Public
                    AppGridLayout.CYLINDER_CAROUSEL -> Icons.Default.ViewCarousel
                    AppGridLayout.HELIX_SPIRAL -> Icons.Default.AutoAwesome
                    AppGridLayout.WAVE_RIBBON -> Icons.Default.GraphicEq
                    AppGridLayout.FLOATING_GRID -> Icons.Default.GridView
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF00E5FF) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = layout.displayName,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                            fontSize = 15.sp
                        )
                        if (layout == AppGridLayout.SPHERE_3D) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PADRÃO",
                                    color = Color(0xFF34D399),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = layout.description,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionado",
                    tint = Color(0xFF00E5FF)
                )
            }
        }
    }
}

