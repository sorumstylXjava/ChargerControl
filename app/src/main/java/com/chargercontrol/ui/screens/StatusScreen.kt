package com.chargercontrol.ui.screens

import android.widget.Toast 
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape 
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext 
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargercontrol.ui.components.*
import com.chargercontrol.utils.RootUtils
import kotlinx.coroutines.delay

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    var batteryData by remember { mutableStateOf(mapOf("level" to 0, "volt" to 0, "temp" to 0, "curr" to 0)) }
    val currentHistory = remember { mutableStateListOf<Int>() }
    val ColorOrange = Color(0xFFFFA500)
    val ColorPink = Color(0xFFFF69B4)

    LaunchedEffect(Unit) {
        while (true) {
            val v = RootUtils.readSmart("volt").toIntOrNull() ?: 0
            val c = RootUtils.readSmart("current").toIntOrNull() ?: 0
            val t = RootUtils.readSmart("temp").toIntOrNull() ?: 0
            val l = RootUtils.readSmart("level").toIntOrNull() ?: 0
            
            batteryData = mapOf("level" to l, "volt" to v/1000, "temp" to t/10, "curr" to c/1000)
            currentHistory.add(c/1000)
            if (currentHistory.size > 20) currentHistory.removeAt(0)
            delay(1000)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(1) }) {
            LargeBatteryView(batteryData["level"] ?: 0)
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusCard(Icons.Default.Timer, "Discharging Time", "measuring", iconColor = Color.Red)
                StatusCard(Icons.Default.SettingsInputComponent, "Voltage", "${batteryData["volt"]}", "mV", Color.Blue)
            }
        }

        item { StatusCard(Icons.Default.BatteryFull, "Current Capacity", "3850", "mAh", Color.Magenta) }
        item { StatusCard(Icons.Default.Thermostat, "Temperature", "${batteryData["temp"]}", "°C", ColorOrange) } // Pake variabel
        item { StatusCard(Icons.Default.Favorite, "Battery Health", "Good", iconColor = ColorPink) } // Pake variabel
        item { StatusCard(Icons.Default.Settings, "Battery Type", "Li-poly", iconColor = Color.Cyan) }

        item(span = { GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Icon(Icons.Default.Bolt, null, tint = Color.Blue)
                            Text("Current", color = Color.Gray, fontSize = 12.sp)
                            Text("${batteryData["curr"]} mA", fontSize = 24.sp, color = Color.White)
                        }
                        val watt = ( (batteryData["volt"] ?: 0).toFloat() * (batteryData["curr"] ?: 0).toFloat() / 1000000f )
                        Text("Watt: %.2f W".format(watt), color = Color.White)
                    }
                    RealTimeGraph(currentHistory.toList())
                }
            }
        }
    }
}
