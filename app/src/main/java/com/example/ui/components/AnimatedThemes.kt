package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.ThemeType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class TouchRipple(
    val id: Long,
    val x: Float,
    val y: Float,
    var radius: Float = 0f,
    var alpha: Float = 1f,
    val color: Color
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float,
    val color: Color
)

@Composable
fun InteractiveThemeContainer(
    currentTheme: ThemeType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val backgroundBrush = when (currentTheme) {
        ThemeType.CYBER_MATRIX -> Brush.verticalGradient(
            listOf(Color(0xFF080C14), Color(0xFF030508), Color(0xFF020305))
        )
        ThemeType.COSMIC_NEBULA -> Brush.radialGradient(
            listOf(Color(0xFF140D26), Color(0xFF0A0714), Color(0xFF040308))
        )
        ThemeType.ZEN_AURORA -> Brush.verticalGradient(
            listOf(Color(0xFF05171D), Color(0xFF040F13), Color(0xFF020709))
        )
        ThemeType.RETRO_ARCADE -> Brush.verticalGradient(
            listOf(Color(0xFF180824), Color(0xFF0C0312), Color(0xFF040106))
        )
        ThemeType.BIO_FOREST -> Brush.radialGradient(
            listOf(Color(0xFF061B12), Color(0xFF04100B), Color(0xFF020604))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        content()
    }
}

// 1. Cyber Matrix Canvas
@Composable
fun CyberMatrixAnimatedCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_grid")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrix_pulse"
    )

    val ripples = remember { mutableStateListOf<TouchRipple>() }
    var touchPos by remember { mutableStateOf(Offset(-100f, -100f)) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    touchPos = offset
                    ripples.add(
                        TouchRipple(
                            id = System.currentTimeMillis() + Random.nextInt(1000),
                            x = offset.x,
                            y = offset.y,
                            radius = 10f,
                            alpha = 1f,
                            color = Color(0xFF00F0FF)
                        )
                    )
                    if (ripples.size > 8) ripples.removeAt(0)
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Dark digital background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF050811),
                    Color(0xFF0B132B),
                    Color(0xFF04060E)
                )
            )
        )

        // Grid lines
        val gridSize = 64f
        val cols = (w / gridSize).toInt() + 1
        val rows = (h / gridSize).toInt() + 1

        for (i in 0..cols) {
            val x = i * gridSize
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
        }
        for (j in 0..rows) {
            val y = (j * gridSize + (pulse * gridSize)) % h
            drawLine(
                color = Color(0xFF00FF66).copy(alpha = 0.07f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        // Falling digital data streams
        val streamCols = 10
        for (s in 0 until streamCols) {
            val sx = (s + 0.5f) * (w / streamCols)
            val sy = ((s * 237f + pulse * h * 1.5f) % (h + 200f)) - 100f
            drawCircle(
                color = Color(0xFF00F0FF).copy(alpha = 0.6f),
                radius = 3.5f,
                center = Offset(sx, sy)
            )
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF00F0FF).copy(alpha = 0.35f)),
                    startY = sy - 80f,
                    endY = sy
                ),
                start = Offset(sx, sy - 80f),
                end = Offset(sx, sy),
                strokeWidth = 2.5f
            )
        }

        // Interactive Ripples
        val iterator = ripples.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            r.radius += 6f
            r.alpha -= 0.025f
            if (r.alpha <= 0f) {
                iterator.remove()
            } else {
                drawCircle(
                    color = r.color.copy(alpha = r.alpha),
                    radius = r.radius,
                    center = Offset(r.x, r.y),
                    style = Stroke(width = 2.5f)
                )
                drawCircle(
                    color = Color(0xFF00FF66).copy(alpha = r.alpha * 0.5f),
                    radius = r.radius * 0.6f,
                    center = Offset(r.x, r.y),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}

// 2. Cosmic Nebula Canvas
@Composable
fun CosmicNebulaAnimatedCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic")
    val cosmicProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_pulse"
    )

    val particles = remember {
        mutableStateListOf<Particle>().apply {
            repeat(35) {
                add(
                    Particle(
                        x = Random.nextFloat() * 1080f,
                        y = Random.nextFloat() * 2400f,
                        vx = (Random.nextFloat() - 0.5f) * 0.6f,
                        vy = (Random.nextFloat() - 0.5f) * 0.6f,
                        radius = Random.nextFloat() * 2.5f + 1f,
                        alpha = Random.nextFloat() * 0.7f + 0.3f,
                        color = if (Random.nextBoolean()) Color(0xFFFFE600) else Color(0xFFB388FF)
                    )
                )
            }
        }
    }

    val ripples = remember { mutableStateListOf<TouchRipple>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    ripples.add(
                        TouchRipple(
                            id = System.currentTimeMillis(),
                            x = offset.x,
                            y = offset.y,
                            radius = 5f,
                            alpha = 1f,
                            color = Color(0xFFD500F9)
                        )
                    )
                    repeat(8) {
                        particles.add(
                            Particle(
                                x = offset.x,
                                y = offset.y,
                                vx = (Random.nextFloat() - 0.5f) * 4f,
                                vy = (Random.nextFloat() - 0.5f) * 4f,
                                radius = Random.nextFloat() * 3.5f + 1.5f,
                                alpha = 1f,
                                color = Color(0xFF80D8FF)
                            )
                        )
                    }
                    if (particles.size > 80) {
                        repeat(10) { particles.removeAt(0) }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Deep space gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2A114E),
                    Color(0xFF140827),
                    Color(0xFF07040E)
                ),
                center = Offset(w * 0.7f, h * 0.3f),
                radius = w * 1.2f
            )
        )

        // Cosmic dust cloud glow
        val cloudGlowX = w * 0.4f + cos(cosmicProgress) * 40f
        val cloudGlowY = h * 0.6f + sin(cosmicProgress) * 30f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF7B1FA2).copy(alpha = 0.22f), Color.Transparent),
                center = Offset(cloudGlowX, cloudGlowY),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(cloudGlowX, cloudGlowY)
        )

        // Particles / Stars
        particles.forEach { p ->
            p.x = (p.x + p.vx + w) % w
            p.y = (p.y + p.vy + h) % h
            drawCircle(
                color = p.color.copy(alpha = (p.alpha * (0.6f + 0.4f * sin(cosmicProgress + p.x))).coerceIn(0.1f, 1f)),
                radius = p.radius,
                center = Offset(p.x, p.y)
            )
        }

        // Ripples
        val ripIter = ripples.iterator()
        while (ripIter.hasNext()) {
            val r = ripIter.next()
            r.radius += 5f
            r.alpha -= 0.02f
            if (r.alpha <= 0f) {
                ripIter.remove()
            } else {
                drawCircle(
                    color = r.color.copy(alpha = r.alpha),
                    radius = r.radius,
                    center = Offset(r.x, r.y),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

// 3. Zen Liquid Aurora Canvas
@Composable
fun ZenAuroraAnimatedCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "zen_aurora")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_wave"
    )

    val ripples = remember { mutableStateListOf<TouchRipple>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    ripples.add(
                        TouchRipple(
                            id = System.currentTimeMillis(),
                            x = offset.x,
                            y = offset.y,
                            radius = 8f,
                            alpha = 0.9f,
                            color = Color(0xFF00F5D4)
                        )
                    )
                    if (ripples.size > 6) ripples.removeAt(0)
                }
            }
    ) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF051923),
                    Color(0xFF003554),
                    Color(0xFF006466)
                )
            )
        )

        // Wave Layer 1
        val path1 = Path().apply {
            moveTo(0f, h * 0.4f)
            for (x in 0..w.toInt() step 20) {
                val y = h * 0.45f + sin(x * 0.008f + wavePhase) * 60f + cos(x * 0.003f) * 30f
                lineTo(x.toFloat(), y)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path1,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF00F5D4).copy(alpha = 0.25f), Color(0xFF051923).copy(alpha = 0.8f))
            )
        )

        // Wave Layer 2
        val path2 = Path().apply {
            moveTo(0f, h * 0.58f)
            for (x in 0..w.toInt() step 20) {
                val y = h * 0.6f + sin(x * 0.006f - wavePhase * 1.2f) * 75f
                lineTo(x.toFloat(), y)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF7B2CBF).copy(alpha = 0.28f), Color(0xFF003554).copy(alpha = 0.7f))
            )
        )

        // Ripples
        val ripIter = ripples.iterator()
        while (ripIter.hasNext()) {
            val r = ripIter.next()
            r.radius += 4f
            r.alpha -= 0.018f
            if (r.alpha <= 0f) ripIter.remove()
            else {
                drawCircle(
                    color = r.color.copy(alpha = r.alpha),
                    radius = r.radius,
                    center = Offset(r.x, r.y),
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

// 4. Retro 80s Arcade Canvas
@Composable
fun RetroArcadeAnimatedCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "retro_grid")
    val scrollOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_scroll"
    )

    val ripples = remember { mutableStateListOf<TouchRipple>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    ripples.add(
                        TouchRipple(
                            id = System.currentTimeMillis(),
                            x = offset.x,
                            y = offset.y,
                            radius = 6f,
                            alpha = 1f,
                            color = Color(0xFFFF007F)
                        )
                    )
                    if (ripples.size > 6) ripples.removeAt(0)
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.52f

        // Sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0D0221), Color(0xFF26083C), Color(0xFF5B1055)),
                startY = 0f,
                endY = horizonY
            ),
            size = androidx.compose.ui.geometry.Size(w, horizonY)
        )

        // Retro synthwave sun
        val sunRadius = w * 0.28f
        val sunCenter = Offset(w * 0.5f, horizonY - sunRadius * 0.45f)
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFEA00), Color(0xFFFF007F)),
                startY = sunCenter.y - sunRadius,
                endY = sunCenter.y + sunRadius
            ),
            radius = sunRadius,
            center = sunCenter
        )

        // Sun horizontal blinds
        for (i in 1..6) {
            val sy = sunCenter.y + (i * 12f)
            if (sy < horizonY) {
                drawLine(
                    color = Color(0xFF26083C),
                    start = Offset(sunCenter.x - sunRadius, sy),
                    end = Offset(sunCenter.x + sunRadius, sy),
                    strokeWidth = 3.5f + (i * 1.5f)
                )
            }
        }

        // Perspective Floor
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1B032D), Color(0xFF090111)),
                startY = horizonY,
                endY = h
            ),
            topLeft = Offset(0f, horizonY),
            size = androidx.compose.ui.geometry.Size(w, h - horizonY)
        )

        // Vanishing lines
        val floorLines = 14
        for (i in 0..floorLines) {
            val bottomX = (i / floorLines.toFloat()) * w
            drawLine(
                color = Color(0xFF00F0FF).copy(alpha = 0.45f),
                start = Offset(w * 0.5f, horizonY),
                end = Offset(bottomX, h),
                strokeWidth = 1.5f
            )
        }

        // Moving horizontal lines
        val hLines = 8
        for (j in 0 until hLines) {
            val fraction = ((j.toFloat() + scrollOffset) / hLines).let { it * it }
            val y = horizonY + fraction * (h - horizonY)
            drawLine(
                color = Color(0xFFFF007F).copy(alpha = 0.5f * fraction),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.8f
            )
        }

        // Interactive Ripples
        val ripIter = ripples.iterator()
        while (ripIter.hasNext()) {
            val r = ripIter.next()
            r.radius += 5.5f
            r.alpha -= 0.02f
            if (r.alpha <= 0f) ripIter.remove()
            else {
                drawCircle(
                    color = r.color.copy(alpha = r.alpha),
                    radius = r.radius,
                    center = Offset(r.x, r.y),
                    style = Stroke(width = 2.5f)
                )
            }
        }
    }
}

// 5. Bio-Luminous Forest Canvas
@Composable
fun BioForestAnimatedCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "bio_forest")
    val fireflyGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "firefly_pulse"
    )

    val fireflies = remember {
        mutableStateListOf<Particle>().apply {
            repeat(28) {
                add(
                    Particle(
                        x = Random.nextFloat() * 1080f,
                        y = Random.nextFloat() * 2400f,
                        vx = (Random.nextFloat() - 0.5f) * 1.2f,
                        vy = (Random.nextFloat() - 0.5f) * 1.2f,
                        radius = Random.nextFloat() * 3f + 2f,
                        alpha = Random.nextFloat() * 0.6f + 0.4f,
                        color = Color(0xFF6EE7B7)
                    )
                )
            }
        }
    }

    var attractOffset by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> attractOffset = offset },
                    onDragEnd = { attractOffset = null },
                    onDragCancel = { attractOffset = null },
                    onDrag = { change, _ ->
                        change.consume()
                        attractOffset = change.position
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    attractOffset = offset
                    repeat(6) {
                        fireflies.add(
                            Particle(
                                x = offset.x + (Random.nextFloat() - 0.5f) * 40f,
                                y = offset.y + (Random.nextFloat() - 0.5f) * 40f,
                                vx = (Random.nextFloat() - 0.5f) * 2f,
                                vy = (Random.nextFloat() - 0.5f) * 2f,
                                radius = 3.5f,
                                alpha = 1f,
                                color = Color(0xFFA7F3D0)
                            )
                        )
                    }
                    if (fireflies.size > 60) repeat(6) { fireflies.removeAt(0) }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Deep midnight forest gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF03140E),
                    Color(0xFF07241A),
                    Color(0xFF030E0A)
                )
            )
        )

        // Soft bioluminescent ambient patch
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF10B981).copy(alpha = 0.14f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.7f),
                radius = w * 0.8f
            ),
            radius = w * 0.8f,
            center = Offset(w * 0.5f, h * 0.7f)
        )

        // Fireflies with interactive physics
        fireflies.forEach { ff ->
            val target = attractOffset
            if (target != null) {
                val dx = target.x - ff.x
                val dy = target.y - ff.y
                ff.vx += (dx * 0.0015f).coerceIn(-1.5f, 1.5f)
                ff.vy += (dy * 0.0015f).coerceIn(-1.5f, 1.5f)
            } else {
                ff.vx += (Random.nextFloat() - 0.5f) * 0.2f
                ff.vy += (Random.nextFloat() - 0.5f) * 0.2f
            }
            ff.vx = ff.vx.coerceIn(-2.5f, 2.5f)
            ff.vy = ff.vy.coerceIn(-2.5f, 2.5f)

            ff.x = (ff.x + ff.vx + w) % w
            ff.y = (ff.y + ff.vy + h) % h

            val glow = (fireflyGlow * ff.alpha).coerceIn(0.15f, 1f)
            // Outer glow
            drawCircle(
                color = ff.color.copy(alpha = glow * 0.35f),
                radius = ff.radius * 3f,
                center = Offset(ff.x, ff.y)
            )
            // Core
            drawCircle(
                color = Color.White.copy(alpha = glow),
                radius = ff.radius,
                center = Offset(ff.x, ff.y)
            )
        }
    }
}
