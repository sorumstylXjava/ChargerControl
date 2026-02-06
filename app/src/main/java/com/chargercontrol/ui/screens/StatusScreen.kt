package com.chargercontrol.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chargercontrol.ui.components.*
import com.chargercontrol.utils.BatteryControl

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    var level by remember { mutableStateOf(0) }
    var temp by remember { mutableStateOf(0f) }
    var volt by remember { mutableStateOf(0) }
    var health by remember { mutableStateOf("") }
    var tech by remember { mutableStateOf("") }
    var currentRaw by remember { mutableStateOf(0) }
    var powerSource by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        while(true) {
            val intent = context.registerReceiver(null, filter)
            intent?.let {
                level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                volt = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                tech = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
                
                val h = it.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
                health = when(h) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    else -> "Normal"
                }

                val ps = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                powerSource = if(ps == BatteryManager.BATTERY_PLUGGED_AC) "Wall Charger" else if(ps == BatteryManager.BATTERY_PLUGGED_USB) "USB Port" else "Battery"
                
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                currentRaw = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(2) }) { LargeBatteryView(level) }
        
        item { StatusCard(Icons.Rounded.Thermostat, "Suhu", "$temp", "°C", Color.Yellow) }
        item { StatusCard(Icons.Rounded.Bolt, "Arus", "${BatteryControl.formatCurrent(currentRaw)}", "mA", Color.Cyan) }
        item { StatusCard(Icons.Rounded.FlashOn, "Voltase", "$volt", "mV", Color.Green) }
        item { StatusCard(Icons.Rounded.HealthAndSafety, "Kesehatan", health, "", Color.Red) }
        item { StatusCard(Icons.Rounded.Memory, "Teknologi", tech, "", Color.Magenta) }
        item { StatusCard(Icons.Rounded.Power, "Sumber", powerSource, "", Color.White) }
        
        item(span = { GridItemSpan(2) }) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Real-time Current Graph", color = Color.Gray, fontSize = 12.sp)
                    RealTimeGraph(listOf(10, 20, currentRaw/10000, 40, 30)) 
                }
            }
        }
    }
}
