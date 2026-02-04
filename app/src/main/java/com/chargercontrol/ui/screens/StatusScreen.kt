package com.chargercontrol.ui.screens

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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargercontrol.utils.RootUtils
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun StatusScreen() {
    var voltage by remember { mutableStateOf("0 mV") }
    var current by remember { mutableStateOf("0 mA") }
    var temp by remember { mutableStateOf("0 °C") }

    LaunchedEffect(Unit) {
        while (true) {
            val v = RootUtils.readSystemFile("/sys/class/power_supply/battery/voltage_now").toIntOrNull() ?: 0
            val c = RootUtils.readSystemFile("/sys/class/power_supply/battery/current_now").toIntOrNull() ?: 0
            val t = RootUtils.readSystemFile("/sys/class/power_supply/battery/temp").toIntOrNull() ?: 0
            
            voltage = "${v / 1000} mV"
            current = "${c / 1000} mA" 
            temp = "${t / 10} °C"
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray)
        ) {
            WaveAnimation()
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(current, fontSize = 32.sp, color = Color.White)
                Text("Current Flow", fontSize = 14.sp, color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoCard("Voltage", voltage)
            InfoCard("Temperature", temp)
        }
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    Card(
        modifier = Modifier.width(160.dp).height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp)
            Text(title, fontSize = 12.sp)
        }
    }
}

@Composable
fun WaveAnimation() {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val waveHeight = 30.dp.toPx()
        val waveWidth = size.width
        val midHeight = size.height / 2

        path.moveTo(0f, midHeight)

        for (x in 0..waveWidth.toInt()) {
            val y = midHeight + sin((x + translateAnim) * 0.02f) * waveHeight
            path.lineTo(x.toFloat(), y.toFloat())
        }

        path.lineTo(waveWidth, size.height)
        path.lineTo(0f, size.height)
        path.close()

        drawPath(path = path, color = Color(0xFF2196F3).copy(alpha = 0.5f), style = Fill)
    }
}
