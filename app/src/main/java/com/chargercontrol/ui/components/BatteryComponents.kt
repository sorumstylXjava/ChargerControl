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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Icon(icon, null, tint = iconColor)
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text("$value $unit", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun RealTimeGraph(points: List<Float>) {
    if (points.isEmpty()) return

    val current = points.lastOrNull() ?: 0f
    val max = points.maxOrNull() ?: 0f
    val min = points.minOrNull() ?: 0f
    val graphColor = Color(0xFF2196F3)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Icon(androidx.compose.material.icons.Icons.Rounded.Bolt, null, tint = graphColor, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Current", color = Color.Gray, fontSize = 14.sp)
                    Text("${current.toInt()} mA", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Max", color = graphColor, fontSize = 14.sp)
                    Text("${max.toInt()} mA", color = Color.White, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Min", color = graphColor, fontSize = 14.sp)
                    Text("${min.toInt()} mA", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                if (points.size < 2) return@Canvas
                
                val path = Path()
                val range = if (max - min == 0f) 1f else max - min
                
                points.forEachIndexed { i, p ->
                    val x = (i.toFloat() / (points.size - 1)) * size.width
                    val normalizedY = (p - min) / range
                    val y = size.height - (normalizedY * size.height)
                    
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                
                drawPath(
                    path = path,
                    color = graphColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}
