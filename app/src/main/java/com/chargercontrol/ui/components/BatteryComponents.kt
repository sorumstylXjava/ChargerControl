package com.chargercontrol.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun LargeBatteryView(level: Int) {
    val infinite = rememberInfiniteTransition(label = "")
    val wave by infinite.animateFloat(
        initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)), label = ""
    )

    Box(
        modifier = Modifier.height(200.dp).width(120.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            val levelHeight = h * (1 - (level / 100f))
            val path = Path().apply {
                moveTo(0f, h)
                lineTo(w, h)
                lineTo(w, levelHeight)
                for (x in 0..w.toInt()) {
                    lineTo(x.toFloat(), levelHeight + 10f * sin((x / 30f) + wave))
                }
                lineTo(0f, levelHeight)
                close()
            }
            drawPath(path, Color(0xFF00E676))
        }
        Text("$level%", color = Color.White, fontSize = 32.sp)
    }
}

@Composable
fun StatusCard(icon: ImageVector, title: String, value: String, unit: String = "", iconColor: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.Gray, fontSize = 11.sp)
            Text("$value $unit", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun RealTimeGraph(points: List<Float>) {
    Canvas(Modifier.fillMaxWidth().height(60.dp)) {
        if (points.size < 2) return@Canvas
        val path = Path()
        val max = points.maxOrNull() ?: 1f
        val min = points.minOrNull() ?: 0f
        val range = if (max - min == 0f) 1f else max - min
        points.forEachIndexed { i, p ->
            val x = (i.toFloat() / (points.size - 1)) * size.width
            val y = size.height - ((p - min) / range) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color(0xFF2196F3), style = Stroke(2.dp.toPx()))
    }
}
