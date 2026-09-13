package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.FontType
import com.example.data.model.IconShape
import kotlin.math.cos
import kotlin.math.sin

fun getShapeForIcon(shape: IconShape): Shape {
    return when (shape) {
        IconShape.CIRCLE -> CircleShape
        IconShape.SQUIRCLE -> RoundedCornerShape(22.dp)
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(12.dp)
        IconShape.HEXAGON -> GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.75f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h * 0.75f)
            lineTo(0f, h * 0.25f)
            close()
        }
        IconShape.TEARDROP -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
    }
}

fun drawableToBitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null
    if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Composable
fun AppGrid(
    apps: List<AppItem>,
    iconShape: IconShape,
    iconSizeDp: Int,
    showLabels: Boolean,
    fontType: FontType,
    columns: Int,
    isEditMode: Boolean,
    getAppIcon: (String, String) -> Drawable?,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onMoveLeft: (AppItem) -> Unit,
    onMoveRight: (AppItem) -> Unit,
    onRemoveFromHome: (AppItem) -> Unit,
    onEditLabel: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = getShapeForIcon(iconShape)
    val labelFont = getFontFamily(fontType)
    val colCount = columns.coerceIn(3, 5)
    val chunkedApps = remember(apps, colCount) { apps.chunked(colCount) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        chunkedApps.forEachIndexed { rowIndex, rowApps ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowApps.forEachIndexed { colIndex, item ->
                    val index = rowIndex * colCount + colIndex
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AppIconItem(
                            app = item,
                            shape = shape,
                            iconSizeDp = iconSizeDp,
                            showLabels = showLabels,
                            labelFont = labelFont,
                            isEditMode = isEditMode,
                            canMoveLeft = index > 0,
                            canMoveRight = index < apps.size - 1,
                            getIcon = { getAppIcon(item.packageName, item.activityName) },
                            onClick = { onAppClick(item) },
                            onLongClick = { onAppLongClick(item) },
                            onMoveLeft = { onMoveLeft(item) },
                            onMoveRight = { onMoveRight(item) },
                            onRemove = { onRemoveFromHome(item) },
                            onEditLabel = { onEditLabel(item) }
                        )
                    }
                }
                // Fill empty slots if last row has fewer items than columns
                val emptySlots = colCount - rowApps.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AppIconItem(
    app: AppItem,
    shape: Shape,
    iconSizeDp: Int,
    showLabels: Boolean,
    labelFont: androidx.compose.ui.text.font.FontFamily,
    isEditMode: Boolean,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    getIcon: () -> Drawable?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRemove: () -> Unit,
    onEditLabel: () -> Unit
) {
    val bitmap = remember(app.uniqueKey) { drawableToBitmap(getIcon()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClick = {
                    if (isEditMode) onEditLabel() else onClick()
                }
            )
            .padding(vertical = 4.dp)
            .testTag("app_item_${app.packageName}")
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(iconSizeDp.dp)
                    .clip(shape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), shape),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = app.displayLabel,
                        modifier = Modifier
                            .size((iconSizeDp * 0.85f).dp)
                            .clip(shape)
                    )
                } else {
                    // Fallback letter avatar
                    Text(
                        text = app.displayLabel.take(1).uppercase(),
                        fontSize = (iconSizeDp * 0.45f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Edit badge (if in edit mode)
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover da Tela Inicial",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Reorder arrows in edit mode
        if (isEditMode) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canMoveLeft) {
                    IconButton(
                        onClick = onMoveLeft,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Mover para esquerda",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onEditLabel,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Renomear",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (canMoveRight) {
                    IconButton(
                        onClick = onMoveRight,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Mover para direita",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (showLabels) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.displayLabel,
                fontFamily = labelFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
