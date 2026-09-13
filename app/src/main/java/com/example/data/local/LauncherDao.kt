package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    @Query("SELECT * FROM launcher_apps WHERE isHidden = 0 ORDER BY sortOrder ASC, label ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM launcher_apps WHERE isPinned = 1 AND isHidden = 0 ORDER BY sortOrder ASC")
    fun getPinnedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM launcher_apps WHERE isHidden = 1 ORDER BY label ASC")
    fun getHiddenApps(): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Upsert
    suspend fun upsertApp(app: AppEntity)

    @Update
    suspend fun updateApp(app: AppEntity)

    @Query("UPDATE launcher_apps SET sortOrder = :order WHERE `key` = :key")
    suspend fun updateAppSortOrder(key: String, order: Int)

    @Query("UPDATE launcher_apps SET customLabel = :customLabel WHERE `key` = :key")
    suspend fun updateCustomLabel(key: String, customLabel: String)

    @Query("UPDATE launcher_apps SET isPinned = :isPinned WHERE `key` = :key")
    suspend fun updatePinned(key: String, isPinned: Boolean)

    @Query("UPDATE launcher_apps SET isHidden = :isHidden WHERE `key` = :key")
    suspend fun updateHidden(key: String, isHidden: Boolean)

    @Query("UPDATE launcher_apps SET clickCount = clickCount + 1 WHERE `key` = :key")
    suspend fun incrementClickCount(key: String)

    // Preference
    @Query("SELECT * FROM launcher_preferences WHERE id = 'default_settings' LIMIT 1")
    fun getPreferences(): Flow<LauncherPreferenceEntity?>

    @Upsert
    suspend fun savePreferences(preferences: LauncherPreferenceEntity)

    // Backups
    @Query("SELECT * FROM launcher_backups ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<BackupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupEntity): Long

    @Query("DELETE FROM launcher_backups WHERE id = :id")
    suspend fun deleteBackup(id: Long)
}
