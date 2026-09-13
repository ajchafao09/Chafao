package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.core.content.ContextCompat

@Composable
fun PermissionsOnboardingDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("launcher_app_prefs", Context.MODE_PRIVATE)
    }

    var hasMic by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCall by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasSms by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasContacts by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasMic = result[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        hasCall = result[Manifest.permission.CALL_PHONE] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        hasSms = result[Manifest.permission.SEND_SMS] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        hasContacts = result[Manifest.permission.READ_CONTACTS] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        sharedPrefs.edit().putBoolean("has_configured_voice_permissions", true).apply()
    }

    val allGranted = hasMic && hasCall && hasSms && hasContacts

    AlertDialog(
        onDismissRequest = {
            sharedPrefs.edit().putBoolean("has_configured_voice_permissions", true).apply()
            onDismiss()
        },
        containerColor = Color(0xFF0F111E),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedLiliOrb(modifier = Modifier.size(52.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Controle Total por Voz com Lili",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Para que a Lili possa ligar, mandar mensagens e abrir aplicativos por comando de voz, ative os acessos abaixo:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Permission Item 1: Mic
                PermissionRequirementItem(
                    icon = Icons.Default.Mic,
                    title = "Microfone e Voz",
                    description = "Ouvir e responder aos seus comandos de voz",
                    isGranted = hasMic,
                    tint = Color(0xFFA78BFA)
                )

                // Permission Item 2: Calls
                PermissionRequirementItem(
                    icon = Icons.Default.Call,
                    title = "Chamadas Telefônicas",
                    description = "Fazer ligações diretas ao pedir 'Ligar para...'",
                    isGranted = hasCall,
                    tint = Color(0xFF34D399)
                )

                // Permission Item 3: Messages / SMS
                PermissionRequirementItem(
                    icon = Icons.AutoMirrored.Filled.Message,
                    title = "Mensagens & SMS",
                    description = "Ditar e enviar mensagens SMS ou WhatsApp",
                    isGranted = hasSms,
                    tint = Color(0xFF60A5FA)
                )

                // Permission Item 4: Contacts
                PermissionRequirementItem(
                    icon = Icons.Default.Contacts,
                    title = "Agenda de Contatos",
                    description = "Localizar números pelo nome dos contatos",
                    isGranted = hasContacts,
                    tint = Color(0xFFF472B6)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!allGranted) {
                        permissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_CONTACTS
                            )
                        )
                    } else {
                        sharedPrefs.edit().putBoolean("has_configured_voice_permissions", true).apply()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grant_all_permissions_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allGranted) Color(0xFF10B981) else Color(0xFF8B5CF6)
                )
            ) {
                Text(
                    text = if (allGranted) "Tudo Pronto! Continuar" else "Conceder Todos os Acessos",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            if (!allGranted) {
                TextButton(
                    onClick = {
                        sharedPrefs.edit().putBoolean("has_configured_voice_permissions", true).apply()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "Configurar Depois",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun PermissionRequirementItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isGranted) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Concedido",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
