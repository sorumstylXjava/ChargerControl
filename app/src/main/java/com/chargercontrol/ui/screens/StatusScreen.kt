package com.chargercontrol.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargercontrol.ui.components.*
import kotlin.math.abs

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    var level by remember { mutableStateOf(0) }
    var voltage by remember { mutableStateOf(0) }
    var temp by remember { mutableStateOf(0f) }
    var currentNow by remember { mutableStateOf(0L) }
    var status by remember { mutableStateOf(0) }
    var health by remember { mutableStateOf("Unknown") }
    var technology by remember { mutableStateOf("Unknown") }
    val graphData = remember { mutableStateListOf<Float>() }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
                
                val h = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                health = when(h) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    else -> "Weak"
                }

                val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                currentNow = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                
                val currentMA = currentNow / 1000f
                graphData.add(currentMA)
                if (graphData.size > 20) graphData.removeAt(0)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val voltageV = voltage / 1000f
    val currentA = currentNow / 1000000f
    
    var watt = voltageV * currentA
    watt = if (isCharging) abs(watt) else -abs(watt)
    
    val currentDisplay = currentNow / 1000 

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080808))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LargeBatteryView(percentage = level)
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.width(180.dp)
            ) {
                StatusCard(Icons.Rounded.Timer, "Discharging Time", if(isCharging) "Charging" else "Measuring", "", Color.Red)
                StatusCard(Icons.Rounded.Bolt, "Voltage", "$voltage", "mV", Color.Blue)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { StatusCard(Icons.Rounded.BatteryFull, "Capacity", "Real-time", "", Color.Magenta) }
            item { StatusCard(Icons.Rounded.Thermostat, "Temperature", "$temp", "°C", Color(0xFFFFA500)) }
            item { StatusCard(Icons.Rounded.Favorite, "Health", health, "", Color(0xFFFF69B4)) }
            item { StatusCard(Icons.Rounded.Settings, "Type", technology, "", Color.Cyan) }
            
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Current", color = Color.Gray, fontSize = 12.sp)
                                Text("${currentDisplay} mA", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text("Watt: ${String.format("%.2f", watt)} W", color = Color.White, fontSize = 16.sp)
                        }
                        RealTimeGraph(points = graphData)
                    }
                }
            }
        }
    }
}
