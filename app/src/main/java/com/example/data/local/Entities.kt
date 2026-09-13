package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_apps")
data class AppEntity(
    @PrimaryKey
    val key: String, // packageName/activityName
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String = "",
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false,
    val clickCount: Int = 0
)

@Entity(tableName = "launcher_backups")
data class BackupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val timestamp: Long,
    val layoutJson: String,
    val deviceModel: String,
    val isCloudSynced: Boolean = true
)

@Entity(tableName = "launcher_preferences")
data class LauncherPreferenceEntity(
    @PrimaryKey
    val id: String = "default_settings",
    val theme: String = "CYBER_MATRIX",
    val clockType: String = "MINIMAL_DIGITAL",
    val fontType: String = "SANS",
    val clockFontType: String = "MONOSPACE",
    val iconShape: String = "SQUIRCLE",
    val iconSize: Int = 60, // dp
    val customWallpaperUri: String? = null,
    val presetWallpaperId: String = "default_cyber",
    val showAppLabels: Boolean = true,
    val gridColumns: Int = 4,
    val showMusicWidget: Boolean = true,
    val showNotesWidget: Boolean = false,
    val showMonitorWidget: Boolean = true,
    val showTogglesWidget: Boolean = true,
    val showForecastWidget: Boolean = false,
    val stickyNoteContent: String = "Bem-vindo ao Lili Launcher! Toque e segure para personalizar.",
    val customCityName: String = "São Paulo",
    val appGridLayout: String = "SPHERE_3D",
    val lastCloudSyncTimestamp: Long = 0L,
    val clockAlignment: String = "CENTER",
    val clockVerticalOffset: Int = 0,
    val clockHorizontalOffset: Int = 0,
    val clockScale: Float = 1.0f,
    val showPhotoVideoWidget: Boolean = false,
    val photoWidgetUri: String? = null,
    val photoWidgetMode: String = "LOOP_CYBER",
    val showCalendarWidget: Boolean = false,
    val showQuoteWidget: Boolean = false
)
