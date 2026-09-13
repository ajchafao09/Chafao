package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem

@Composable
fun AppActionDialog(
    app: AppItem,
    appIcon: Drawable?,
    onRemoveFromHome: () -> Unit,
    onRename: () -> Unit,
    onEnterEditMode: () -> Unit,
    onHideApp: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(appIcon) { drawableToBitmap(appIcon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111827),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1F2937)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = app.displayLabel,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Text(
                            text = app.displayLabel.take(1).uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.displayLabel,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp,
                        maxLines = 1
                    )
                    Text(
                        text = app.packageName,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Option 1: Remove from Home Screen
                AppActionItemRow(
                    icon = Icons.Default.Delete,
                    title = "Remover da Tela Inicial",
                    subtitle = "Oculta da tela inicial (continua no menu de apps)",
                    tint = Color(0xFFF87171),
                    onClick = {
                        onRemoveFromHome()
                        onDismiss()
                    }
                )

                // Option 1.5: Hide App from Launcher Completely
                AppActionItemRow(
                    icon = Icons.Default.VisibilityOff,
                    title = "Ocultar Aplicativo",
                    subtitle = "Esconde este app do launcher (menu e tela inicial)",
                    tint = Color(0xFFF59E0B),
                    onClick = {
                        onHideApp()
                        onDismiss()
                    }
                )

                // Option 2: Rename App
                AppActionItemRow(
                    icon = Icons.Default.Edit,
                    title = "Renomear Atalho",
                    subtitle = "Personalize o nome exibido deste app",
                    tint = Color(0xFF60A5FA),
                    onClick = {
                        onRename()
                        onDismiss()
                    }
                )

                // Option 3: App Info (System Settings)
                AppActionItemRow(
                    icon = Icons.Default.Info,
                    title = "Informações do Aplicativo",
                    subtitle = "Permissões, armazenamento e notificações",
                    tint = Color(0xFF34D399),
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        onDismiss()
                    }
                )

                // Option 4: Uninstall App
                AppActionItemRow(
                    icon = Icons.Default.DeleteForever,
                    title = "Desinstalar do Dispositivo",
                    subtitle = "Remove o aplicativo completamente",
                    tint = Color(0xFFFB7185),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DELETE).apply {
                                data = Uri.parse("package:${app.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        onDismiss()
                    }
                )

                // Option 5: Reorganize / Edit Mode
                AppActionItemRow(
                    icon = Icons.Default.SwapHoriz,
                    title = "Modo de Organização",
                    subtitle = "Reordenar posição dos ícones na tela",
                    tint = Color(0xFFA78BFA),
                    onClick = {
                        onEnterEditMode()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Fechar",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
private fun AppActionItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}
