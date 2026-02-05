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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.* // Ini penting agar semua icon terbaca
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chargercontrol.ui.components.*

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    var level by remember { mutableStateOf(0) }
    var volt by remember { mutableStateOf(0) }
    var temp by remember { mutableStateOf(0f) }
    var curr by remember { mutableStateOf(0L) }
    val history = remember { mutableStateListOf<Float>() }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                volt = i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                temp = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                val bm = c.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                curr = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                history.add(curr / 1000f)
                if (history.size > 30) history.removeAt(0)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                LargeBatteryView(level = level) // Memanggil dengan parameter 'level'
            }
        }
        item { StatusCard(Icons.Rounded.Thermostat, "Temp", "$temp", "°C", Color.Yellow) }
        item { StatusCard(Icons.Rounded.Bolt, "Voltage", "$volt", "mV", Color.Cyan) }
        item { StatusCard(Icons.Rounded.History, "Update", "Real-time", "", Color.Green) }
        item { StatusCard(Icons.Rounded.BatteryFull, "Health", "Good", "", Color.Magenta) }
        
        item(span = { GridItemSpan(2) }) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))) {
                Column(Modifier.padding(16.dp)) {
                    Text("Current: ${curr / 1000} mA", color = Color.White)
                    RealTimeGraph(history)
                }
            }
        }
    }
}
