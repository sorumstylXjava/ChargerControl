package com.chargercontrol.ui.components

import androidx.compose.ui.graphics.drawscope.clipRect
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun LargeBatteryView(percentage: Int) {
    val transition = rememberInfiniteTransition()
    val waveOffset by transition.animateFloat(
        initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing))
    )

    Box(
        modifier = Modifier.size(width = 140.dp, height = 220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bodyHeight = size.height * 0.9f
            val headWidth = size.width * 0.3f
            val headHeight = size.height * 0.08f

            drawRoundRect(
                color = Color.DarkGray,
                topLeft = Offset((size.width - headWidth) / 2, 0f),
                size = Size(headWidth, headHeight),
                cornerRadius = CornerRadius(10f)
            )

            drawRoundRect(
                color = Color(0xFF1E1E1E),
                topLeft = Offset(0f, headHeight),
                size = Size(size.width, bodyHeight),
                cornerRadius = CornerRadius(30f)
            )

            clipRect(top = headHeight, bottom = size.height) {
                val fillHeight = bodyHeight * (percentage / 100f)
                val topY = headHeight + (bodyHeight - fillHeight)
                
                val path = Path().apply {
                    moveTo(0f, topY)
                    for (x in 0..size.width.toInt()) {
                        val y = topY + sin(x * 0.05f + waveOffset) * 10f
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = if (percentage > 20) Color(0xFF4CAF50) else Color.Red)
            }
        }
        Text("${percentage}%", fontSize = 32.sp, color = Color.White)
    }
}

@Composable
fun StatusCard(icon: ImageVector, title: String, value: String, unit: String = "", iconColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text("$value $unit", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun RealTimeGraph(points: List<Int>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 16.dp)) {
        if (points.size < 2) return@Canvas
        val path = Path()
        val xStep = size.width / (points.size - 1)
        val maxVal = (points.maxOrNull() ?: 1).toFloat()
        val minVal = (points.minOrNull() ?: 0).toFloat()
        val range = if (maxVal == minVal) 1f else maxVal - minVal

        points.forEachIndexed { i, p ->
            val x = i * xStep
            val y = size.height - ((p - minVal) / range * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = Color(0xFF2196F3), style = Stroke(width = 4f))
    }
}
