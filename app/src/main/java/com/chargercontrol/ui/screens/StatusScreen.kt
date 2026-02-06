package com.chargercontrol.ui.screens

import android.content.Context
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

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    var level by remember { mutableStateOf(0) }
    var temp by remember { mutableStateOf(0f) }
    var volt by remember { mutableStateOf(0) } 
    var health by remember { mutableStateOf("Unknown") }
    var tech by remember { mutableStateOf("Li-ion") }
    var currentRaw by remember { mutableStateOf(0) }
    var powerSource by remember { mutableStateOf("Battery") }
    var capacity by remember { mutableStateOf(0) }
    
    val currentHistory = remember { mutableStateListOf<Int>() }

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
                powerSource = when(ps) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> "Discharging"
                }
                
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val now = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                currentRaw = now
                
                capacity = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
                
                val currentMA = BatteryControl.formatCurrent(now)
                currentHistory.add(currentMA)
                if (currentHistory.size > 30) currentHistory.removeAt(0)
            }
            delay(1000) 
        }
    }

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
                LargeBatteryView(percentage = level)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (powerSource == "Discharging") "BATTERY DISCHARGING" else "FAST CHARGING ACTIVE",
                    color = if (powerSource == "Discharging") Color.Gray else Color(0xFF00E676),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        item(span = { GridItemSpan(2) }) {
            val watts = (volt.toFloat() / 1000f) * (BatteryControl.formatCurrent(currentRaw).toFloat() / 1000f)
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
                        Text("Charging Power", color = Color.Gray, fontSize = 12.sp)
                        Text("${String.format("%.2f", watts)} Watt", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Icon(Icons.Rounded.ElectricBolt, null, tint = Color.Yellow, modifier = Modifier.size(40.dp))
                }
            }
        }

        item { StatusCard(Icons.Rounded.Thermostat, "Suhu", "$temp", "°C", Color.Yellow) }
        item { StatusCard(Icons.Rounded.Bolt, "Arus", "${BatteryControl.formatCurrent(currentRaw)}", "mA", Color.Cyan) }
        item { StatusCard(Icons.Rounded.FlashOn, "Voltase", "$volt", "mV", Color.Green) }
        item { StatusCard(Icons.Rounded.Favorite, "Kesehatan", health, "", Color(0xFFFF4081)) }
        item { StatusCard(Icons.Rounded.Memory, "Teknologi", tech, "", Color.Magenta) }
        item { StatusCard(Icons.Rounded.Power, "Sumber", powerSource, "", Color.White) }
        item { StatusCard(Icons.Rounded.Straighten, "Kapasitas", "$capacity", "mAh", Color.Orange) }
        item { StatusCard(Icons.Rounded.History, "Cycle", "N/A", "", Color.LightGray) }
        
        item(span = { GridItemSpan(2) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = 80.dp) 
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Live Current Consumption (mA)", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    RealTimeGraph(points = currentHistory.toList()) 
                }
            }
        }
    }
}
