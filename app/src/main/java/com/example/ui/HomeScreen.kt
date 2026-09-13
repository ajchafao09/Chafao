package com.example.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assistant.LiliAssistantManager
import com.example.data.model.AppGridLayout
import com.example.data.model.AppItem
import com.example.data.model.ClockType
import com.example.data.model.FontType
import com.example.data.model.IconShape
import com.example.data.model.ThemeType
import com.example.ui.components.AnimatedAppLayoutContainer
import com.example.ui.components.AppActionDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.ClockWidget
import com.example.ui.components.EditLabelDialog
import com.example.ui.components.DeviceStatsWidget
import com.example.ui.components.InteractiveThemeContainer
import com.example.ui.components.LiliAssistantDialog
import com.example.ui.components.LiliQuoteWidget
import com.example.ui.components.MediaQuickWidget
import com.example.ui.components.MiniCalendarWidget
import com.example.ui.components.PermissionsOnboardingDialog
import com.example.ui.components.PhotoVideoSquareWidget
import com.example.ui.components.QuickTogglesWidget
import com.example.ui.components.StickyNotesWidget
import com.example.ui.components.TopStatusWeatherBar
import com.example.ui.settings.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pinnedApps by viewModel.pinnedApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val hiddenApps by viewModel.hiddenApps.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val backups by viewModel.backups.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val drawerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val liliSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var selectedAppForActions by remember { mutableStateOf<AppItem?>(null) }
    val liliManager = remember { LiliAssistantManager(context) }
    var isTorchActive by remember { mutableStateOf(false) }

    // Detect if music is actively playing on the device
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }
    var isDeviceMusicPlaying by remember { mutableStateOf(false) }
    var isManualMusicPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isDeviceMusicPlaying = audioManager?.isMusicActive == true
            delay(1200)
        }
    }

    val isMusicPlaying = isDeviceMusicPlaying || isManualMusicPlaying

    // Request Default Launcher Role Contract
    val defaultHomeRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

    fun requestDefaultLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    defaultHomeRoleLauncher.launch(intent)
                    return
                }
            }
        }
        // Fallback to Home Settings
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    val sharedPrefs = remember {
        context.getSharedPreferences("launcher_app_prefs", Context.MODE_PRIVATE)
    }

    val hasConfiguredPermissions = remember {
        sharedPrefs.getBoolean("has_configured_voice_permissions", false)
    }

    var showPermissionsDialog by remember {
        mutableStateOf(!hasConfiguredPermissions)
    }

    // Default launcher check & dialog prompt
    var showDefaultLauncherDialog by remember {
        mutableStateOf(hasConfiguredPermissions && !com.example.MainActivity.isDefaultLauncher(context))
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

    val currentFont = try {
        FontType.valueOf(preferences.fontType)
    } catch (_: Exception) {
        FontType.SANS
    }

    val currentClockFont = try {
        FontType.valueOf(preferences.clockFontType)
    } catch (_: Exception) {
        FontType.MONOSPACE
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

    // Full screen view: Settings Screen if open
    if (uiState.isSettingsOpen) {
        SettingsScreen(
            currentPreferences = preferences,
            weatherInfo = uiState.weather,
            backups = backups,
            apps = pinnedApps,
            hiddenApps = hiddenApps,
            getAppIcon = { pkg, act -> viewModel.getAppIcon(pkg, act) },
            onUpdatePreferences = { viewModel.updatePreferences(it) },
            onCreateBackup = { viewModel.createBackup(it) },
            onRestoreBackup = { viewModel.restoreBackup(it) },
            onDeleteBackup = { viewModel.deleteBackup(it) },
            onUnhideApp = { viewModel.unhideApp(it) },
            onUnhideAllApps = { viewModel.unhideAllApps() },
            onRequestSetDefaultHome = { requestDefaultLauncher() },
            onBack = { viewModel.openSettings(false) }
        )
        return
    }

    val clockAlignmentEnum = when (preferences.clockAlignment) {
        "START" -> Alignment.CenterStart
        "END" -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    InteractiveThemeContainer(
        currentTheme = currentTheme,
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopStatusWeatherBar(
                    weatherInfo = uiState.weather,
                    onOpenLili = { viewModel.openLili(true) },
                    onOpenSettings = { viewModel.openSettings(true) },
                    onWeatherClick = { viewModel.loadWeather() }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Clock Widget with custom positioning & scaling
                ClockWidget(
                    clockType = currentClock,
                    fontType = currentClockFont,
                    weatherInfo = uiState.weather,
                    alignment = clockAlignmentEnum,
                    verticalOffsetDp = preferences.clockVerticalOffset,
                    horizontalOffsetDp = preferences.clockHorizontalOffset,
                    scale = preferences.clockScale
                )

                // Music Player: appears when music is actively playing or widget enabled
                AnimatedVisibility(
                    visible = (isMusicPlaying || preferences.showMusicWidget),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    MediaQuickWidget(
                        isPlaying = isMusicPlaying,
                        onTogglePlay = {
                            isManualMusicPlaying = !isMusicPlaying
                        }
                    )
                }

                // Photo / Looping Video Square Widget
                if (preferences.showPhotoVideoWidget) {
                    Box(modifier = Modifier.padding(vertical = 4.dp)) {
                        PhotoVideoSquareWidget(
                            customPhotoUri = preferences.photoWidgetUri,
                            mode = preferences.photoWidgetMode,
                            onSelectPhoto = { uri ->
                                viewModel.updatePreferences(
                                    preferences.copy(
                                        photoWidgetUri = uri.toString(),
                                        photoWidgetMode = "CUSTOM_PHOTO"
                                    )
                                )
                            },
                            onChangeMode = { newMode ->
                                viewModel.updatePreferences(
                                    preferences.copy(photoWidgetMode = newMode)
                                )
                            }
                        )
                    }
                }

                // Mini Calendar Widget
                if (preferences.showCalendarWidget) {
                    MiniCalendarWidget()
                }

                // Lili AI Quote Widget
                if (preferences.showQuoteWidget) {
                    LiliQuoteWidget()
                }

                // Device Stats Widget
                if (preferences.showMonitorWidget) {
                    DeviceStatsWidget(
                        batteryLevel = 85,
                        freeRamMb = 3400,
                        totalRamMb = 8192,
                        freeStorageGb = 64,
                        totalStorageGb = 128
                    )
                }

                // Quick Toggles Widget
                if (preferences.showTogglesWidget) {
                    QuickTogglesWidget(
                        isTorchOn = isTorchActive,
                        onToggleTorch = {
                            isTorchActive = !isTorchActive
                            liliManager.toggleFlashlight(isTorchActive)
                        }
                    )
                }

                // Sticky Notes Widget
                if (preferences.showNotesWidget) {
                    StickyNotesWidget(
                        initialText = preferences.stickyNoteContent,
                        onSaveText = { newText ->
                            viewModel.updatePreferences(preferences.copy(stickyNoteContent = newText))
                        }
                    )
                }

                // Edit Mode Indicator
                AnimatedVisibility(
                    visible = uiState.isEditMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✨ Modo de Edição Ativo: Use as setas para reorganizar os ícones ou toque no ícone para renomear.",
                            color = Color(0xFFA7F3D0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Main Animated Apps Grid / Interactive 3D Sphere
                // Centered according to the user's phone dimensions without screen clutter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedAppLayoutContainer(
                        apps = pinnedApps,
                        layout = currentAppLayout,
                        iconShape = currentShape,
                        iconSizeDp = preferences.iconSize,
                        showLabels = preferences.showAppLabels,
                        fontType = currentFont,
                        columns = preferences.gridColumns,
                        isEditMode = uiState.isEditMode,
                        getAppIcon = { pkg, act -> viewModel.getAppIcon(pkg, act) },
                        onAppClick = { viewModel.launchApp(it) },
                        onAppLongClick = { selectedAppForActions = it },
                        onMoveLeft = { viewModel.moveAppLeft(it) },
                        onMoveRight = { viewModel.moveAppRight(it) },
                        onRemoveFromHome = { viewModel.toggleAppPin(it) },
                        onEditLabel = { viewModel.setEditingApp(it) }
                    )
                }
            }
        }
    }

    // App Action Context Menu Dialog (Remove from home, Rename, Info, Uninstall, Reorganize)
    selectedAppForActions?.let { targetApp ->
        AppActionDialog(
            app = targetApp,
            appIcon = viewModel.getAppIcon(targetApp.packageName, targetApp.activityName),
            onRemoveFromHome = {
                viewModel.toggleAppPin(targetApp)
            },
            onRename = {
                viewModel.setEditingApp(targetApp)
            },
            onEnterEditMode = {
                viewModel.setEditMode(true)
            },
            onHideApp = {
                viewModel.hideApp(targetApp)
            },
            onDismiss = {
                selectedAppForActions = null
            }
        )
    }

    // App Drawer Bottom Sheet
    if (uiState.isAppDrawerOpen) {
        AppDrawerSheet(
            allApps = allApps,
            iconShape = currentShape,
            fontType = currentFont,
            sheetState = drawerSheetState,
            getAppIcon = { pkg, act -> viewModel.getAppIcon(pkg, act) },
            onLaunchApp = { viewModel.launchApp(it) },
            onTogglePin = { viewModel.toggleAppPin(it) },
            onAppLongClick = { selectedAppForActions = it },
            onDismiss = {
                scope.launch {
                    drawerSheetState.hide()
                    viewModel.openAppDrawer(false)
                }
            }
        )
    }

    // Lili AI Assistant Dialog Sheet
    if (uiState.isLiliOpen) {
        LiliAssistantDialog(
            sheetState = liliSheetState,
            isProcessing = uiState.isLiliProcessing,
            lastResponse = uiState.liliLastResponse,
            onSendCommand = { viewModel.processLiliCommand(it) },
            onRequestPermissions = {
                showPermissionsDialog = true
            },
            onDismiss = {
                scope.launch {
                    liliSheetState.hide()
                    viewModel.openLili(false)
                }
            }
        )
    }

    // Voice & System Permissions Onboarding Dialog
    if (showPermissionsDialog) {
        PermissionsOnboardingDialog(
            onDismiss = {
                showPermissionsDialog = false
                if (!com.example.MainActivity.isDefaultLauncher(context)) {
                    showDefaultLauncherDialog = true
                }
            }
        )
    }

    // Edit App Label Dialog
    uiState.editingAppItem?.let { appToEdit ->
        EditLabelDialog(
            app = appToEdit,
            onConfirm = { newName ->
                viewModel.updateAppLabel(appToEdit, newName)
            },
            onDismiss = { viewModel.setEditingApp(null) }
        )
    }

    // Default Launcher Permission Dialog (shown whenever accessed if not default)
    if (showDefaultLauncherDialog) {
        AlertDialog(
            onDismissRequest = {
                showDefaultLauncherDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Definir como Launcher Principal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Deseja definir o Lili Launcher como sua tela inicial padrão? Dessa forma, todos os seus atalhos 3D, assistente IA, clima e temas estarão sempre prontos ao tocar no botão de início do celular.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDefaultLauncherDialog = false
                        requestDefaultLauncher()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Definir como Padrão",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDefaultLauncherDialog = false
                    }
                ) {
                    Text(
                        text = "Agora Não",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            },
            containerColor = Color(0xFF1E1B4B),
            shape = RoundedCornerShape(24.dp)
        )
    }
}
