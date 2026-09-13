package com.example.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistant.LiliAction
import com.example.assistant.LiliAssistantManager
import com.example.assistant.LiliResponse
import com.example.data.local.BackupEntity
import com.example.data.local.LauncherDatabase
import com.example.data.local.LauncherPreferenceEntity
import com.example.data.model.AppItem
import com.example.data.model.ClockType
import com.example.data.model.FontType
import com.example.data.model.ThemeType
import com.example.data.model.WeatherInfo
import com.example.data.repository.LauncherRepository
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val isEditMode: Boolean = false,
    val isAppDrawerOpen: Boolean = false,
    val isLiliOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val editingAppItem: AppItem? = null,
    val isLiliProcessing: Boolean = false,
    val liliLastResponse: String? = null,
    val isTorchOn: Boolean = false,
    val batteryPercent: Int = 85,
    val weather: WeatherInfo = WeatherInfo(
        temperature = 24,
        condition = "Ensolarado",
        city = "São Paulo",
        humidity = 58,
        iconEmoji = "☀️",
        highTemp = 28,
        lowTemp = 18
    )
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LauncherDatabase.getDatabase(application)
    private val repository = LauncherRepository(application, db.launcherDao())
    private val weatherRepository = WeatherRepository(application)
    private val assistantManager = LiliAssistantManager(application)

    val pinnedApps: StateFlow<List<AppItem>> = repository.getPinnedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApps: StateFlow<List<AppItem>> = repository.getAllApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenApps: StateFlow<List<AppItem>> = repository.getHiddenApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<LauncherPreferenceEntity> = repository.getPreferences()
        .map { it ?: LauncherPreferenceEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherPreferenceEntity())

    val backups: StateFlow<List<BackupEntity>> = repository.getAllBackups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.syncInstalledApps()
        }
        loadWeather()
        updateBatteryLevel()
    }

    fun getAppIcon(packageName: String, activityName: String): Drawable? {
        return repository.getAppIcon(packageName, activityName)
    }

    fun launchApp(app: AppItem) {
        viewModelScope.launch {
            repository.launchApp(app)
        }
    }

    fun moveAppLeft(app: AppItem) {
        viewModelScope.launch {
            val list = pinnedApps.value
            val currentIndex = list.indexOfFirst { it.uniqueKey == app.uniqueKey }
            if (currentIndex > 0) {
                val target = list[currentIndex - 1]
                val currentOrder = app.sortOrder
                val targetOrder = target.sortOrder
                repository.updateAppSortOrder(app.uniqueKey, targetOrder)
                repository.updateAppSortOrder(target.uniqueKey, currentOrder)
            }
        }
    }

    fun moveAppRight(app: AppItem) {
        viewModelScope.launch {
            val list = pinnedApps.value
            val currentIndex = list.indexOfFirst { it.uniqueKey == app.uniqueKey }
            if (currentIndex >= 0 && currentIndex < list.size - 1) {
                val target = list[currentIndex + 1]
                val currentOrder = app.sortOrder
                val targetOrder = target.sortOrder
                repository.updateAppSortOrder(app.uniqueKey, targetOrder)
                repository.updateAppSortOrder(target.uniqueKey, currentOrder)
            }
        }
    }

    fun toggleAppPin(app: AppItem) {
        viewModelScope.launch {
            repository.setAppPinned(app.uniqueKey, !app.isPinned)
        }
    }

    fun updateAppLabel(app: AppItem, newLabel: String) {
        viewModelScope.launch {
            repository.setAppCustomLabel(app.uniqueKey, newLabel)
            _uiState.value = _uiState.value.copy(editingAppItem = null)
        }
    }

    fun setEditMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isEditMode = enabled)
    }

    fun openAppDrawer(open: Boolean) {
        _uiState.value = _uiState.value.copy(isAppDrawerOpen = open)
    }

    fun openLili(open: Boolean) {
        _uiState.value = _uiState.value.copy(isLiliOpen = open)
    }

    fun openSettings(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSettingsOpen = open)
    }

    fun setEditingApp(app: AppItem?) {
        _uiState.value = _uiState.value.copy(editingAppItem = app)
    }

    fun updatePreferences(newPrefs: LauncherPreferenceEntity) {
        viewModelScope.launch {
            repository.savePreferences(newPrefs)
            if (newPrefs.customCityName != preferences.value.customCityName) {
                loadWeather(newPrefs.customCityName)
            }
        }
    }

    fun saveStickyNotes(text: String) {
        viewModelScope.launch {
            repository.savePreferences(preferences.value.copy(stickyNoteContent = text))
        }
    }

    fun toggleFlashlight() {
        val newState = assistantManager.toggleFlashlight()
        _uiState.value = _uiState.value.copy(isTorchOn = assistantManager.isFlashlightOn())
    }

    fun loadWeather(customCity: String? = null) {
        viewModelScope.launch {
            val city = customCity ?: preferences.value.customCityName
            val weather = weatherRepository.fetchWeather(customCity = city)
            _uiState.value = _uiState.value.copy(weather = weather)
        }
    }

    private fun updateBatteryLevel() {
        val bm = getApplication<Application>().getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
        _uiState.value = _uiState.value.copy(batteryPercent = level)
    }

    fun processLiliCommand(userCommand: String, speakOutput: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLiliOpen = true,
                isLiliProcessing = true
            )
            val response: LiliResponse = assistantManager.processCommand(
                userPrompt = userCommand,
                allApps = allApps.value,
                currentWeather = _uiState.value.weather
            )
            _uiState.value = _uiState.value.copy(
                isLiliProcessing = false,
                liliLastResponse = response.message,
                isTorchOn = assistantManager.isFlashlightOn()
            )

            if (speakOutput) {
                assistantManager.speak(response.message)
            }

            // Execute Lili Action
            when (val action = response.action) {
                is LiliAction.LaunchApp -> {
                    repository.launchApp(action.app)
                    _uiState.value = _uiState.value.copy(isLiliOpen = false)
                }
                is LiliAction.MakeCall -> {
                    val app = getApplication<Application>()
                    val phoneUri = Uri.parse("tel:${action.phoneNumber}")
                    val hasCallPerm = ContextCompat.checkSelfPermission(app, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                    try {
                        val intent = if (hasCallPerm) {
                            Intent(Intent.ACTION_CALL, phoneUri)
                        } else {
                            Intent(Intent.ACTION_DIAL, phoneUri)
                        }.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        app.startActivity(intent)
                    } catch (_: Exception) {
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL, phoneUri).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            app.startActivity(dialIntent)
                        } catch (_: Exception) {}
                    }
                }
                is LiliAction.SendMessage -> {
                    val app = getApplication<Application>()
                    if (action.isWhatsApp) {
                        try {
                            val cleanNumber = action.phoneNumber?.replace(Regex("[^0-9+]"), "") ?: ""
                            val uri = if (cleanNumber.isNotBlank()) {
                                Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(action.message)}")
                            } else {
                                Uri.parse("whatsapp://send?text=${Uri.encode(action.message)}")
                            }
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            app.startActivity(intent)
                        } catch (_: Exception) {
                            // Fallback to SMS if WhatsApp isn't installed
                            try {
                                val smsUri = Uri.parse("smsto:${action.phoneNumber ?: ""}")
                                val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                    putExtra("sms_body", action.message)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                app.startActivity(smsIntent)
                            } catch (_: Exception) {}
                        }
                    } else {
                        // Standard SMS
                        try {
                            val smsUri = Uri.parse("smsto:${action.phoneNumber ?: ""}")
                            val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                putExtra("sms_body", action.message)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            app.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("sms:${action.phoneNumber ?: ""}")
                                    putExtra("sms_body", action.message)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                app.startActivity(sendIntent)
                            } catch (_: Exception) {}
                        }
                    }
                }
                is LiliAction.ToggleEditMode -> {
                    _uiState.value = _uiState.value.copy(isEditMode = true, isLiliOpen = false)
                }
                is LiliAction.OpenAppDrawer -> {
                    _uiState.value = _uiState.value.copy(isAppDrawerOpen = true, isLiliOpen = false)
                }
                is LiliAction.ChangeTheme -> {
                    repository.savePreferences(preferences.value.copy(theme = action.theme.name))
                }
                is LiliAction.ChangeClock -> {
                    repository.savePreferences(preferences.value.copy(clockType = action.clock.name))
                }
                is LiliAction.ChangeFont -> {
                    repository.savePreferences(preferences.value.copy(fontType = action.font.name))
                }
                is LiliAction.ToggleFlashlight -> {
                    _uiState.value = _uiState.value.copy(isTorchOn = assistantManager.isFlashlightOn())
                }
                is LiliAction.OpenSettings -> {
                    try {
                        val intent = Intent(action.action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        getApplication<Application>().startActivity(intent)
                    } catch (_: Exception) {}
                }
                is LiliAction.SearchWeb -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(action.query)}")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        getApplication<Application>().startActivity(intent)
                    } catch (_: Exception) {}
                }
                is LiliAction.HideApp -> {
                    hideApp(action.app)
                }
                LiliAction.None -> {}
            }
        }
    }

    fun createBackup(title: String) {
        viewModelScope.launch {
            repository.createCloudBackup(title, preferences.value, pinnedApps.value)
        }
    }

    fun hideApp(app: AppItem) {
        viewModelScope.launch {
            repository.setAppHidden(app.uniqueKey, true)
        }
    }

    fun unhideApp(app: AppItem) {
        viewModelScope.launch {
            repository.setAppHidden(app.uniqueKey, false)
        }
    }

    fun unhideAllApps() {
        viewModelScope.launch {
            repository.unhideAllApps()
        }
    }

    fun restoreBackup(backup: BackupEntity) {
        viewModelScope.launch {
            repository.restoreBackup(backup)
        }
    }

    fun deleteBackup(backupId: Long) {
        viewModelScope.launch {
            repository.deleteBackup(backupId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        assistantManager.shutdown()
    }
}
