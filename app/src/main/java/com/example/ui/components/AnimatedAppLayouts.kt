@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.AppGridLayout
import com.example.data.model.AppItem
import com.example.data.model.FontType
import com.example.data.model.IconShape
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Projected 3D item representation
data class ProjectedAppNode(
    val app: AppItem,
    val originalIndex: Int,
    val x3D: Float,
    val y3D: Float,
    val z3D: Float,
    val screenX: Float,
    val screenY: Float,
    val scale: Float,
    val alpha: Float,
    val isFront: Boolean
)

/**
 * Main Animated Layout Container that renders the selected grid layout
 * Defaults to SPHERE_3D (Esfera 3D totalmente interativa)
 */
@Composable
fun AnimatedAppLayoutContainer(
    apps: List<AppItem>,
    layout: AppGridLayout,
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
    onChangeLayout: (AppGridLayout) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Active Animated Grid Content (Theme/Layout selection now kept exclusively in Settings)
        Crossfade(
            targetState = layout,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "app_layout_crossfade",
            modifier = Modifier.fillMaxSize()
        ) { currentLayout ->
            when (currentLayout) {
                AppGridLayout.SPHERE_3D -> {
                    InteractiveSphereAppGrid(
                        apps = apps,
                        iconShape = iconShape,
                        iconSizeDp = iconSizeDp,
                        showLabels = showLabels,
                        fontType = fontType,
                        isEditMode = isEditMode,
                        getAppIcon = getAppIcon,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onMoveLeft = onMoveLeft,
                        onMoveRight = onMoveRight,
                        onRemoveFromHome = onRemoveFromHome,
                        onEditLabel = onEditLabel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppGridLayout.CYLINDER_CAROUSEL -> {
                    InteractiveCylinderAppGrid(
                        apps = apps,
                        iconShape = iconShape,
                        iconSizeDp = iconSizeDp,
                        showLabels = showLabels,
                        fontType = fontType,
                        isEditMode = isEditMode,
                        getAppIcon = getAppIcon,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onMoveLeft = onMoveLeft,
                        onMoveRight = onMoveRight,
                        onRemoveFromHome = onRemoveFromHome,
                        onEditLabel = onEditLabel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppGridLayout.HELIX_SPIRAL -> {
                    InteractiveHelixAppGrid(
                        apps = apps,
                        iconShape = iconShape,
                        iconSizeDp = iconSizeDp,
                        showLabels = showLabels,
                        fontType = fontType,
                        isEditMode = isEditMode,
                        getAppIcon = getAppIcon,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onMoveLeft = onMoveLeft,
                        onMoveRight = onMoveRight,
                        onRemoveFromHome = onRemoveFromHome,
                        onEditLabel = onEditLabel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppGridLayout.WAVE_RIBBON -> {
                    InteractiveWaveAppGrid(
                        apps = apps,
                        iconShape = iconShape,
                        iconSizeDp = iconSizeDp,
                        showLabels = showLabels,
                        fontType = fontType,
                        isEditMode = isEditMode,
                        getAppIcon = getAppIcon,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onMoveLeft = onMoveLeft,
                        onMoveRight = onMoveRight,
                        onRemoveFromHome = onRemoveFromHome,
                        onEditLabel = onEditLabel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppGridLayout.FLOATING_GRID -> {
                    InteractiveFloatingGrid(
                        apps = apps,
                        iconShape = iconShape,
                        iconSizeDp = iconSizeDp,
                        showLabels = showLabels,
                        fontType = fontType,
                        columns = columns,
                        isEditMode = isEditMode,
                        getAppIcon = getAppIcon,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onMoveLeft = onMoveLeft,
                        onMoveRight = onMoveRight,
                        onRemoveFromHome = onRemoveFromHome,
                        onEditLabel = onEditLabel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. MODELO PRINCIPAL PADRÃO: ESFERA 3D TOTALMENTE INTERATIVA
// -------------------------------------------------------------
@Composable
fun InteractiveSphereAppGrid(
    apps: List<AppItem>,
    iconShape: IconShape,
    iconSizeDp: Int,
    showLabels: Boolean,
    fontType: FontType,
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
    if (apps.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum aplicativo fixado na Esfera 3D.\nAbra a gaveta de apps para fixar.",
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
        return
    }

    var rotY by remember { mutableFloatStateOf(0f) }
    var rotX by remember { mutableFloatStateOf(0.15f) }
    var velocityX by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }
    var isUserInteracting by remember { mutableStateOf(false) }
    var autoRotate by remember { mutableStateOf(true) }

    // Fluid momentum & ambient rotation physics loop (~60 FPS)
    LaunchedEffect(isUserInteracting, autoRotate) {
        if (!isUserInteracting) {
            while (true) {
                val speed = sqrt(velocityX * velocityX + velocityY * velocityY)
                if (speed > 0.0002f) {
                    rotY += velocityX
                    rotX += velocityY
                    // Natural air resistance friction decay
                    velocityX *= 0.945f
                    velocityY *= 0.945f
                } else {
                    velocityX = 0f
                    velocityY = 0f
                    if (autoRotate) {
                        rotY += 0.0032f
                    }
                }
                delay(16)
            }
        }
    }

    val shape = getShapeForIcon(iconShape)
    val labelFont = getFontFamily(fontType)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) {
            if (maxHeight != androidx.compose.ui.unit.Dp.Infinity && maxHeight != androidx.compose.ui.unit.Dp.Unspecified && maxHeight > 0.dp) maxHeight.toPx()
            else widthPx * 1.35f
        }
        val centerX = widthPx / 2f
        val centerY = heightPx / 2f

        // Camera perspective parameters
        val cameraDistance = 3.0f
        val maxPerspective = 1.30f

        val maxIconSizePx = with(density) { (iconSizeDp * 1.15f).dp.toPx() }
        val horizontalSafePadding = with(density) { 14.dp.toPx() }
        val verticalSafePadding = with(density) { 24.dp.toPx() }

        // Expand radius to maximum possible size without any icons passing the phone's screen borders
        val maxSafeRadiusX = ((widthPx / 2f) - (maxIconSizePx / 2f) - horizontalSafePadding) / maxPerspective
        val maxSafeRadiusY = ((heightPx / 2f) - (maxIconSizePx / 2f) - verticalSafePadding) / maxPerspective
        val sphereRadius = minOf(maxSafeRadiusX, maxSafeRadiusY).coerceAtLeast(with(density) { 95.dp.toPx() })

        // Mathematical distribution of apps on 3D sphere surface
        val totalApps = apps.size
        val projectedNodes = remember(apps, rotX, rotY, sphereRadius, centerX, centerY) {
            apps.mapIndexed { index, app ->
                // Fibonacci Golden Spiral distribution on sphere surface
                val yNorm = 1f - (index + 0.5f) * (2f / totalApps.toFloat())
                val clampedY = yNorm.coerceIn(-1f, 1f)
                val radiusAtY = sqrt((1f - clampedY * clampedY).coerceAtLeast(0f))
                val goldenAngle = 2.399963229728653f
                val phi = index * goldenAngle

                val x0 = cos(phi) * radiusAtY
                val y0 = clampedY
                val z0 = sin(phi) * radiusAtY

                // Rotate around Y axis (rotY)
                val x1 = x0 * cos(rotY) + z0 * sin(rotY)
                val y1 = y0
                val z1 = -x0 * sin(rotY) + z0 * cos(rotY)

                // Rotate around X axis (rotX)
                val x2 = x1
                val y2 = y1 * cos(rotX) - z1 * sin(rotX)
                val z2 = y1 * sin(rotX) + z1 * cos(rotX)

                // 3D Perspective Projection
                val perspective = (cameraDistance / (cameraDistance - z2 * 0.70f)).coerceIn(0.6f, maxPerspective)
                val screenX = centerX + x2 * sphereRadius * perspective
                val screenY = centerY + y2 * sphereRadius * perspective

                // Depth-dependent scale and alpha
                val depthFactor = ((z2 + 1f) / 2f).coerceIn(0f, 1f)
                val scale = (0.50f + 0.55f * depthFactor).coerceIn(0.48f, 1.25f)
                val alpha = (0.25f + 0.75f * depthFactor).coerceIn(0.20f, 1.0f)
                val isFront = z2 > -0.2f

                ProjectedAppNode(
                    app = app,
                    originalIndex = index,
                    x3D = x2,
                    y3D = y2,
                    z3D = z2,
                    screenX = screenX,
                    screenY = screenY,
                    scale = scale,
                    alpha = alpha,
                    isFront = isFront
                )
            }.sortedBy { it.z3D } // Back-to-front rendering order
        }

        // 3D Sphere Interactive Area with Drag and Tap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isUserInteracting = true
                            velocityX = 0f
                            velocityY = 0f
                        },
                        onDragEnd = {
                            isUserInteracting = false
                        },
                        onDragCancel = {
                            isUserInteracting = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val sensitivity = 0.0055f
                            val deltaY = dragAmount.x * sensitivity
                            val deltaX = -dragAmount.y * sensitivity

                            rotY += deltaY
                            rotX += deltaX

                            // Smooth running momentum tracking
                            velocityX = velocityX * 0.35f + deltaY * 0.65f
                            velocityY = velocityY * 0.35f + deltaX * 0.65f
                        }
                    )
                }
                .testTag("interactive_3d_sphere")
        ) {
            // Background Canvas: Soft ambient glow without distracting wireframe lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Sphere glow core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.08f),
                            Color(0xFF8B5CF6).copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = sphereRadius * 1.35f
                    ),
                    radius = sphereRadius * 1.35f,
                    center = Offset(centerX, centerY)
                )
            }

            // Render 3D Projected App Nodes
            projectedNodes.forEach { node ->
                val app = node.app
                val iconSize = (iconSizeDp * node.scale).dp
                val densityScope = LocalDensity.current
                val xOffsetDp = with(densityScope) { node.screenX.toDp() } - (iconSize / 2f)
                val yOffsetDp = with(densityScope) { node.screenY.toDp() } - (iconSize / 2f)

                Box(
                    modifier = Modifier
                        .offset(x = xOffsetDp, y = yOffsetDp)
                        .zIndex(node.z3D + 2f)
                        .graphicsLayer {
                            scaleX = node.scale
                            scaleY = node.scale
                            alpha = node.alpha
                        }
                        .combinedClickable(
                            enabled = node.isFront,
                            onClick = {
                                if (isEditMode) onEditLabel(app) else onAppClick(app)
                            },
                            onLongClick = {
                                onAppLongClick(app)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SphereAppIconNode(
                        app = app,
                        shape = shape,
                        iconSizeDp = iconSizeDp,
                        showLabels = showLabels && node.isFront,
                        labelFont = labelFont,
                        isEditMode = isEditMode,
                        isFront = node.isFront,
                        canMoveLeft = node.originalIndex > 0,
                        canMoveRight = node.originalIndex < totalApps - 1,
                        getIcon = { getAppIcon(app.packageName, app.activityName) },
                        onMoveLeft = { onMoveLeft(app) },
                        onMoveRight = { onMoveRight(app) },
                        onRemove = { onRemoveFromHome(app) },
                        onEditLabel = { onEditLabel(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun SphereAppIconNode(
    app: AppItem,
    shape: Shape,
    iconSizeDp: Int,
    showLabels: Boolean,
    labelFont: androidx.compose.ui.text.font.FontFamily,
    isEditMode: Boolean,
    isFront: Boolean,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    getIcon: () -> Drawable?,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRemove: () -> Unit,
    onEditLabel: () -> Unit
) {
    val bitmap = remember(app.uniqueKey) { drawableToBitmap(getIcon()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width((iconSizeDp + 16).dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Icon container with front glow
            Box(
                modifier = Modifier
                    .size(iconSizeDp.dp)
                    .clip(shape)
                    .background(
                        if (isFront) Color(0xFF0F172A).copy(alpha = 0.95f) else Color(0xFF0F172A).copy(alpha = 0.55f)
                    )
                    .border(
                        width = if (isFront) 1.dp else 0.5.dp,
                        color = if (isFront) Color(0xFF00E5FF).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = app.displayLabel,
                        modifier = Modifier
                            .size((iconSizeDp * 0.82f).dp)
                            .clip(shape)
                    )
                } else {
                    Text(
                        text = app.displayLabel.take(1).uppercase(),
                        fontSize = (iconSizeDp * 0.45f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Edit Mode Controls
            if (isEditMode && isFront) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        if (showLabels && isFront) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = app.displayLabel,
                fontFamily = labelFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// 2. MODELO: CILINDRO 3D HOLOGRÁFICO
// -------------------------------------------------------------
@Composable
fun InteractiveCylinderAppGrid(
    apps: List<AppItem>,
    iconShape: IconShape,
    iconSizeDp: Int,
    showLabels: Boolean,
    fontType: FontType,
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
    var cylinderRotation by remember { mutableFloatStateOf(0f) }
    var isInteracting by remember { mutableStateOf(false) }

    LaunchedEffect(isInteracting) {
        if (!isInteracting) {
            while (true) {
                delay(16)
                cylinderRotation += 0.005f
            }
        }
    }

    val shape = getShapeForIcon(iconShape)
    val labelFont = getFontFamily(fontType)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isInteracting = true },
                    onDragEnd = { isInteracting = false },
                    onDragCancel = { isInteracting = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        cylinderRotation += dragAmount.x * 0.006f
                    }
                )
            }
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val centerX = widthPx / 2f
        val centerY = heightPx / 2f
        val cylinderRadius = widthPx * 0.40f

        val total = apps.size.coerceAtLeast(1)
        val angleStep = (2 * PI / total).toFloat()

        val sortedItems = apps.mapIndexed { index, app ->
            val angle = cylinderRotation + index * angleStep
            val x3D = sin(angle) * cylinderRadius
            val z3D = cos(angle) * cylinderRadius
            val y3D = (index % 2 - 0.5f) * 60f // Staggered two-row cylinder

            val depth = ((z3D + cylinderRadius) / (2f * cylinderRadius)).coerceIn(0f, 1f)
            val scale = 0.55f + 0.50f * depth
            val alpha = 0.30f + 0.70f * depth

            Triple(app, index, ProjectedAppNode(
                app = app,
                originalIndex = index,
                x3D = x3D,
                y3D = y3D,
                z3D = z3D,
                screenX = centerX + x3D,
                screenY = centerY + y3D,
                scale = scale,
                alpha = alpha,
                isFront = z3D > 0f
            ))
        }.sortedBy { it.third.z3D }

        sortedItems.forEach { (_, _, node) ->
            val app = node.app
            val iconSize = (iconSizeDp * node.scale).dp
            val xDp = with(density) { node.screenX.toDp() } - (iconSize / 2f)
            val yDp = with(density) { node.screenY.toDp() } - (iconSize / 2f)

            Box(
                modifier = Modifier
                    .offset(x = xDp, y = yDp)
                    .zIndex(node.z3D + 1000f)
                    .graphicsLayer {
                        scaleX = node.scale
                        scaleY = node.scale
                        alpha = node.alpha
                    }
                    .combinedClickable(
                        enabled = node.isFront,
                        onClick = { if (isEditMode) onEditLabel(app) else onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
            ) {
                SphereAppIconNode(
                    app = app,
                    shape = shape,
                    iconSizeDp = iconSizeDp,
                    showLabels = showLabels && node.isFront,
                    labelFont = labelFont,
                    isEditMode = isEditMode,
                    isFront = node.isFront,
                    canMoveLeft = false,
                    canMoveRight = false,
                    getIcon = { getAppIcon(app.packageName, app.activityName) },
                    onMoveLeft = {},
                    onMoveRight = {},
                    onRemove = { onRemoveFromHome(app) },
                    onEditLabel = { onEditLabel(app) }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. MODELO: ESPIRAL HELIX CÓSMICA 3D
// -------------------------------------------------------------
@Composable
fun InteractiveHelixAppGrid(
    apps: List<AppItem>,
    iconShape: IconShape,
    iconSizeDp: Int,
    showLabels: Boolean,
    fontType: FontType,
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
    var helixOffset by remember { mutableFloatStateOf(0f) }
    var isInteracting by remember { mutableStateOf(false) }

    LaunchedEffect(isInteracting) {
        if (!isInteracting) {
            while (true) {
                delay(16)
                helixOffset += 0.008f
            }
        }
    }

    val shape = getShapeForIcon(iconShape)
    val labelFont = getFontFamily(fontType)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isInteracting = true },
                    onDragEnd = { isInteracting = false },
                    onDragCancel = { isInteracting = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        helixOffset += (dragAmount.y + dragAmount.x) * 0.006f
                    }
                )
            }
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val centerX = widthPx / 2f
        val centerY = heightPx / 2f
        val helixRadius = widthPx * 0.35f

        val total = apps.size.coerceAtLeast(1)
        val step = (4 * PI / total).toFloat()

        val nodes = apps.mapIndexed { index, app ->
            val theta = helixOffset + index * step
            val x = cos(theta) * helixRadius
            val z = sin(theta) * helixRadius
            val progress = (index / total.toFloat()) - 0.5f
            val y = progress * heightPx * 0.70f

            val depth = ((z + helixRadius) / (2f * helixRadius)).coerceIn(0f, 1f)
            val scale = 0.55f + 0.50f * depth
            val alpha = 0.25f + 0.75f * depth

            ProjectedAppNode(
                app = app,
                originalIndex = index,
                x3D = x,
                y3D = y,
                z3D = z,
                screenX = centerX + x,
                screenY = centerY + y,
                scale = scale,
                alpha = alpha,
                isFront = z > 0f
            )
        }.sortedBy { it.z3D }

        nodes.forEach { node ->
            val app = node.app
            val iconSize = (iconSizeDp * node.scale).dp
            val xDp = with(density) { node.screenX.toDp() } - (iconSize / 2f)
            val yDp = with(density) { node.screenY.toDp() } - (iconSize / 2f)

            Box(
                modifier = Modifier
                    .offset(x = xDp, y = yDp)
                    .zIndex(node.z3D + 500f)
                    .graphicsLayer {
                        scaleX = node.scale
                        scaleY = node.scale
                        alpha = node.alpha
                    }
                    .combinedClickable(
                        enabled = node.isFront,
                        onClick = { if (isEditMode) onEditLabel(app) else onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
            ) {
                SphereAppIconNode(
                    app = app,
                    shape = shape,
                    iconSizeDp = iconSizeDp,
                    showLabels = showLabels && node.isFront,
                    labelFont = labelFont,
                    isEditMode = isEditMode,
                    isFront = node.isFront,
                    canMoveLeft = false,
                    canMoveRight = false,
                    getIcon = { getAppIcon(app.packageName, app.activityName) },
                    onMoveLeft = {},
                    onMoveRight = {},
                    onRemove = { onRemoveFromHome(app) },
                    onEditLabel = { onEditLabel(app) }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. MODELO: ONDA FLUTUANTE SENOIDAL
// -------------------------------------------------------------
@Composable
fun InteractiveWaveAppGrid(
    apps: List<AppItem>,
    iconShape: IconShape,
    iconSizeDp: Int,
    showLabels: Boolean,
    fontType: FontType,
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
    val infiniteTransition = rememberInfiniteTransition(label = "wave_oscillation")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val shape = getShapeForIcon(iconShape)
    val labelFont = getFontFamily(fontType)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        val chunkedApps = remember(apps) { apps.chunked(4) }
        chunkedApps.forEachIndexed { rowIndex, rowApps ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowApps.forEachIndexed { colIndex, app ->
                    val index = rowIndex * 4 + colIndex
                    val waveOffset = sin(wavePhase + index * 0.8f) * 14f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                translationY = waveOffset
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppIconItem(
                            app = app,
                            shape = shape,
                            iconSizeDp = iconSizeDp,
                            showLabels = showLabels,
                            labelFont = labelFont,
                            isEditMode = isEditMode,
                            canMoveLeft = index > 0,
                            canMoveRight = index < apps.size - 1,
                            getIcon = { getAppIcon(app.packageName, app.activityName) },
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) },
                            onMoveLeft = { onMoveLeft(app) },
                            onMoveRight = { onMoveRight(app) },
                            onRemove = { onRemoveFromHome(app) },
                            onEditLabel = { onEditLabel(app) }
                        )
                    }
                }
                repeat(4 - rowApps.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
}

// -------------------------------------------------------------
// 5. MODELO: GRADE HOLOGRÁFICA COM INCLINAÇÃO 3D
// -------------------------------------------------------------
@Composable
fun InteractiveFloatingGrid(
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
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    val shape = getShapeForIcon(iconShape)
    val labelFont = getFontFamily(fontType)
    val colCount = columns.coerceIn(3, 5)
    val chunkedApps = remember(apps, colCount) { apps.chunked(colCount) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        tiltX = 0f
                        tiltY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        tiltY = (tiltY + dragAmount.x * 0.05f).coerceIn(-15f, 15f)
                        tiltX = (tiltX - dragAmount.y * 0.05f).coerceIn(-15f, 15f)
                    }
                )
            }
            .graphicsLayer {
                rotationX = tiltX
                rotationY = tiltY
                cameraDistance = 12f * density
            }
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
                val emptySlots = colCount - rowApps.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
}
