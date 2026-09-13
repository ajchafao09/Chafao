package com.example

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.HomeScreen
import com.example.ui.LauncherViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: LauncherViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        val uiState by viewModel.uiState.collectAsState()

        // Handle back button on home screen: close drawer or settings or edit mode
        BackHandler(enabled = uiState.isAppDrawerOpen || uiState.isLiliOpen || uiState.isSettingsOpen || uiState.isEditMode) {
          when {
            uiState.isSettingsOpen -> viewModel.openSettings(false)
            uiState.isLiliOpen -> viewModel.openLili(false)
            uiState.isAppDrawerOpen -> viewModel.openAppDrawer(false)
            uiState.isEditMode -> viewModel.setEditMode(false)
          }
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Color(0xFF060911)
        ) {
          HomeScreen(viewModel = viewModel)
        }
      }
    }
  }

  private fun promptSetDefaultLauncher() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
      if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
        if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
          try {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            startActivity(intent)
            return
          } catch (_: Exception) {}
        }
      }
    }
    try {
      val intent = Intent(Settings.ACTION_HOME_SETTINGS)
      startActivity(intent)
    } catch (_: Exception) {
      try {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        startActivity(intent)
      } catch (_: Exception) {}
    }
  }

  companion object {
    fun isDefaultLauncher(context: Context): Boolean {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
          return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        }
      }
      val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
      val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
      val currentDefaultPackage = resolveInfo?.activityInfo?.packageName
      return currentDefaultPackage == context.packageName
    }
  }
}

