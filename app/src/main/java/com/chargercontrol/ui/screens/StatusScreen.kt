package com.chargercontrol.ui.screens

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import com.chargercontrol.utils.BatteryControl
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    
    var level by remember { mutableStateOf(0) }
    var temp by remember { mutableStateOf(0f) }
    var volt by remember { mutableStateOf(0) }
    var health by remember { mutableStateOf("Good") }
    var tech by remember { mutableStateOf("Li-ion") }
    var currentMA by remember { mutableStateOf(0) }
    var powerSource by remember { mutableStateOf("Battery") }
    var capacity by remember { mutableStateOf(0) }
    var cycleCount by remember { mutableStateOf("N/A") }
    
    val currentHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(Unit) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        while(true) {
            val intent = context.registerReceiver(null, filter)
            intent?.let {
                level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                volt = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                tech = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
                
                health = when(it.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                    else -> "Normal"
                }

                val ps = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                powerSource = if (ps > 0) "Charging" else "Discharging"
                
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                capacity = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
            }

            val rawCurrentUA = withContext(Dispatchers.IO) {
                val paths = listOf(
                    "/sys/class/power_supply/battery/current_now",
                    "/sys/class/power_supply/bms/current_now",
                    "/sys/class/power_supply/main/current_now"
                )
                var current = 0
                for (path in paths) {
                    try {
                        val value = BatteryControl.readNode(path).trim().toIntOrNull()
                        if (value != null && value != 0) {
                            current = value
                            break
                        }
                    } catch (e: Throwable) { }
                }
                current
            }
            
            currentMA = rawCurrentUA / 1000
            
            cycleCount = withContext(Dispatchers.IO) {
                try { BatteryControl.getCycleCount() } catch (e: Throwable) { "N/A" }
            }

            currentHistory.add(currentMA.toFloat())
            if (currentHistory.size > 50) currentHistory.removeAt(0)

            delay(1000) 
        }
    }

    val watts = abs((volt.toFloat() / 1000f) * (currentMA.toFloat() / 1000f))
    val isCharging = currentMA > 0

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LargeBatteryView(level) 
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isCharging) "FAST CHARGING ACTIVE" else "BATTERY DISCHARGING",
                    color = if (isCharging) Color(0xFF00E676) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        item(span = { GridItemSpan(2) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Power Usage", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "${String.format("%.2f", watts)} Watt", 
                            color = if (isCharging) Color(0xFF00E676) else Color(0xFFEF5350), 
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Icon(Icons.Rounded.ElectricBolt, null, tint = Color.Yellow, modifier = Modifier.size(40.dp))
                }
            }
        }

        item { StatusCard(Icons.Rounded.Thermostat, "Suhu", "$temp", "°C", Color.Yellow) }
        item { StatusCard(Icons.Rounded.Bolt, "Arus", "$currentMA", "mA", Color.Cyan) }
        item { StatusCard(Icons.Rounded.FlashOn, "Voltase", "$volt", "mV", Color.Green) }
        item { StatusCard(Icons.Rounded.Favorite, "Kesehatan", health, "", Color(0xFFFF4081)) }
        item { StatusCard(Icons.Rounded.Memory, "Teknologi", tech, "", Color.Magenta) }
        item { StatusCard(Icons.Rounded.Power, "Sumber", powerSource, "", Color.White) }
        item { StatusCard(Icons.Rounded.Straighten, "Kapasitas", "$capacity", "mAh", Color(0xFFFFA500)) }
        item { StatusCard(Icons.Rounded.History, "Cycle Count", cycleCount, "", Color.LightGray) }
        
        item(span = { GridItemSpan(2) }) {
            RealTimeGraph(currentHistory.toList())
        }
        item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
    }
}
