package com.example.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.example.data.local.AppEntity
import com.example.data.local.BackupEntity
import com.example.data.local.LauncherDao
import com.example.data.local.LauncherPreferenceEntity
import com.example.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class LauncherRepository(
    private val context: Context,
    private val dao: LauncherDao
) {
    private val iconCache = ConcurrentHashMap<String, Drawable>()

    fun getAppIcon(packageName: String, activityName: String): Drawable? {
        val key = "$packageName/$activityName"
        return iconCache[key] ?: run {
            try {
                val pm = context.packageManager
                val intent = Intent().setComponent(ComponentName(packageName, activityName))
                val resolveInfo = pm.resolveActivity(intent, 0)
                val icon = resolveInfo?.loadIcon(pm) ?: pm.getApplicationIcon(packageName)
                iconCache[key] = icon
                icon
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun syncInstalledApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val existingEntities = dao.getAllApps().firstOrNull()?.associateBy { it.key } ?: emptyMap()

        val prefs = context.getSharedPreferences("launcher_app_prefs", Context.MODE_PRIVATE)
        val hasMigratedAllPinned = prefs.getBoolean("has_migrated_all_pinned_v2", false)

        val entitiesToInsert = mutableListOf<AppEntity>()
        resolveInfos.forEachIndexed { index, resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val act = resolveInfo.activityInfo.name
            val key = "$pkg/$act"
            val label = resolveInfo.loadLabel(pm).toString()

            // Pre-cache icon
            try {
                val icon = resolveInfo.loadIcon(pm)
                iconCache[key] = icon
            } catch (_: Exception) {}

            val existing = existingEntities[key]
            if (existing != null) {
                // If not migrated to "all apps pinned by default", make them pinned now
                val pinnedState = if (!hasMigratedAllPinned) true else existing.isPinned
                entitiesToInsert.add(existing.copy(label = label, isPinned = pinnedState))
            } else {
                // All installed apps appear by default on home screen, with individual remove option
                entitiesToInsert.add(
                    AppEntity(
                        key = key,
                        packageName = pkg,
                        activityName = act,
                        label = label,
                        customLabel = "",
                        isPinned = true,
                        sortOrder = index,
                        isHidden = false,
                        isFavorite = false,
                        clickCount = 0
                    )
                )
            }
        }

        if (!hasMigratedAllPinned) {
            prefs.edit().putBoolean("has_migrated_all_pinned_v2", true).apply()
        }

        if (entitiesToInsert.isNotEmpty()) {
            dao.insertApps(entitiesToInsert)
        }
    }

    fun getAllApps(): Flow<List<AppItem>> {
        return dao.getAllApps().map { list ->
            list.map { entity ->
                AppItem(
                    packageName = entity.packageName,
                    activityName = entity.activityName,
                    label = entity.label,
                    customLabel = entity.customLabel,
                    isPinned = entity.isPinned,
                    sortOrder = entity.sortOrder,
                    isHidden = entity.isHidden,
                    isFavorite = entity.isFavorite,
                    clickCount = entity.clickCount
                )
            }
        }
    }

    fun getPinnedApps(): Flow<List<AppItem>> {
        return dao.getPinnedApps().map { list ->
            list.map { entity ->
                AppItem(
                    packageName = entity.packageName,
                    activityName = entity.activityName,
                    label = entity.label,
                    customLabel = entity.customLabel,
                    isPinned = entity.isPinned,
                    sortOrder = entity.sortOrder,
                    isHidden = entity.isHidden,
                    isFavorite = entity.isFavorite,
                    clickCount = entity.clickCount
                )
            }
        }
    }

    fun getHiddenApps(): Flow<List<AppItem>> {
        return dao.getHiddenApps().map { list ->
            list.map { entity ->
                AppItem(
                    packageName = entity.packageName,
                    activityName = entity.activityName,
                    label = entity.label,
                    customLabel = entity.customLabel,
                    isPinned = entity.isPinned,
                    sortOrder = entity.sortOrder,
                    isHidden = entity.isHidden,
                    isFavorite = entity.isFavorite,
                    clickCount = entity.clickCount
                )
            }
        }
    }

    fun getPreferences(): Flow<LauncherPreferenceEntity?> = dao.getPreferences()

    suspend fun savePreferences(preferences: LauncherPreferenceEntity) = withContext(Dispatchers.IO) {
        dao.savePreferences(preferences)
    }

    suspend fun launchApp(item: AppItem): Boolean = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(item.packageName, item.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            withContext(Dispatchers.IO) {
                dao.incrementClickCount(item.uniqueKey)
            }
            true
        } catch (_: Exception) {
            // Fallback launch by package name
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(item.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun updateAppSortOrder(key: String, newOrder: Int) = withContext(Dispatchers.IO) {
        dao.updateAppSortOrder(key, newOrder)
    }

    suspend fun reorderPinnedApps(reorderedKeys: List<String>) = withContext(Dispatchers.IO) {
        reorderedKeys.forEachIndexed { index, key ->
            dao.updateAppSortOrder(key, index)
        }
    }

    suspend fun setAppPinned(key: String, pinned: Boolean) = withContext(Dispatchers.IO) {
        dao.updatePinned(key, pinned)
    }

    suspend fun setAppCustomLabel(key: String, label: String) = withContext(Dispatchers.IO) {
        dao.updateCustomLabel(key, label)
    }

    suspend fun setAppHidden(key: String, hidden: Boolean) = withContext(Dispatchers.IO) {
        dao.updateHidden(key, hidden)
    }

    suspend fun unhideAllApps() = withContext(Dispatchers.IO) {
        val hiddenList = dao.getHiddenApps().firstOrNull() ?: emptyList()
        hiddenList.forEach { entity ->
            dao.updateHidden(entity.key, false)
        }
    }

    // Cloud Backup & Restore
    fun getAllBackups(): Flow<List<BackupEntity>> = dao.getAllBackups()

    suspend fun createCloudBackup(title: String, currentPreferences: LauncherPreferenceEntity, pinnedApps: List<AppItem>): Long = withContext(Dispatchers.IO) {
        val root = JSONObject().apply {
            put("version", 1)
            put("theme", currentPreferences.theme)
            put("clockType", currentPreferences.clockType)
            put("fontType", currentPreferences.fontType)
            put("clockFontType", currentPreferences.clockFontType)
            put("iconShape", currentPreferences.iconShape)
            put("iconSize", currentPreferences.iconSize)
            put("presetWallpaperId", currentPreferences.presetWallpaperId)
            put("gridColumns", currentPreferences.gridColumns)
            put("stickyNote", currentPreferences.stickyNoteContent)

            val appsArray = JSONArray()
            pinnedApps.forEach { app ->
                val appObj = JSONObject().apply {
                    put("key", app.uniqueKey)
                    put("pkg", app.packageName)
                    put("act", app.activityName)
                    put("label", app.label)
                    put("customLabel", app.customLabel)
                    put("sortOrder", app.sortOrder)
                }
                appsArray.put(appObj)
            }
            put("pinnedApps", appsArray)
        }

        val deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val entity = BackupEntity(
            title = title,
            timestamp = System.currentTimeMillis(),
            layoutJson = root.toString(),
            deviceModel = deviceName,
            isCloudSynced = true
        )
        dao.insertBackup(entity)
    }

    suspend fun restoreBackup(backup: BackupEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(backup.layoutJson)
            val currentPrefs = dao.getPreferences().firstOrNull() ?: LauncherPreferenceEntity()
            val updatedPrefs = currentPrefs.copy(
                theme = root.optString("theme", currentPrefs.theme),
                clockType = root.optString("clockType", currentPrefs.clockType),
                fontType = root.optString("fontType", currentPrefs.fontType),
                clockFontType = root.optString("clockFontType", currentPrefs.clockFontType),
                iconShape = root.optString("iconShape", currentPrefs.iconShape),
                iconSize = root.optInt("iconSize", currentPrefs.iconSize),
                presetWallpaperId = root.optString("presetWallpaperId", currentPrefs.presetWallpaperId),
                gridColumns = root.optInt("gridColumns", currentPrefs.gridColumns),
                stickyNoteContent = root.optString("stickyNote", currentPrefs.stickyNoteContent),
                lastCloudSyncTimestamp = System.currentTimeMillis()
            )
            dao.savePreferences(updatedPrefs)

            val appsArray = root.optJSONArray("pinnedApps")
            if (appsArray != null) {
                for (i in 0 until appsArray.length()) {
                    val appObj = appsArray.getJSONObject(i)
                    val key = appObj.getString("key")
                    val customLabel = appObj.optString("customLabel", "")
                    val sortOrder = appObj.optInt("sortOrder", i)
                    dao.updatePinned(key, true)
                    dao.updateAppSortOrder(key, sortOrder)
                    if (customLabel.isNotBlank()) {
                        dao.updateCustomLabel(key, customLabel)
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteBackup(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteBackup(id)
    }
}
