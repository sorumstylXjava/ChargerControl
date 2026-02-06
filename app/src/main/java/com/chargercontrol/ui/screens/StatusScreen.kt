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

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    
    var level by remember { mutableStateOf(0) }
    var temp by remember { mutableStateOf(0f) }
    var volt by remember { mutableStateOf(0) }
    var health by remember { mutableStateOf("Good") }
    var tech by remember { mutableStateOf("Li-ion") }
    var currentMA by remember { mutableStateOf(0f) }
    var powerSource by remember { mutableStateOf("Battery") }
    var capacity by remember { mutableStateOf("N/A") }
    var cycleCount by remember { mutableStateOf("N/A") }
    var watts by remember { mutableStateOf(0f) }
    
    val currentHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(Unit) {
        while(true) {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            
            withContext(Dispatchers.IO) {
                val rawCurrent = BatteryControl.getFormattedCurrent(context)
                val rawWatt = BatteryControl.getWattage(context)
                val rawCapacity = BatteryControl.getDesignedCapacity(context)
                val rawCycle = BatteryControl.getCycleCount()
                val rawHealth = BatteryControl.getBatteryHealth(context)
                val rawTech = BatteryControl.getBatteryTechnology(context)

                withContext(Dispatchers.Main) {
                    currentMA = rawCurrent
                    watts = rawWatt
                    capacity = rawCapacity
                    cycleCount = rawCycle
                    health = rawHealth
                    tech = rawTech

                    intent?.let {
                        level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                        volt = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                        
                        val ps = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                        powerSource = if (ps > 0) "Charging" else "Discharging"
                    }

                    currentHistory.add(currentMA)
                    if (currentHistory.size > 50) currentHistory.removeAt(0)
                }
            }
            delay(1000)
        }
    }

    val isCharging = currentMA > 0

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(horizontal = 16.dp),
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
        item { StatusCard(Icons.Rounded.Bolt, "Arus", "${currentMA.toInt()}", "mA", Color.Cyan) }
        item { StatusCard(Icons.Rounded.FlashOn, "Voltase", "$volt", "mV", Color.Green) }
        item { StatusCard(Icons.Rounded.Favorite, "Kesehatan", health, "", Color(0xFFFF4081)) }
        item { StatusCard(Icons.Rounded.Memory, "Teknologi", tech, "", Color.Magenta) }
        item { StatusCard(Icons.Rounded.Power, "Sumber", powerSource, "", Color.White) }
        item { StatusCard(Icons.Rounded.Straighten, "Kapasitas", capacity, "", Color(0xFFFFA500)) }
        item { StatusCard(Icons.Rounded.History, "Cycle Count", cycleCount, "", Color.LightGray) }
        
        item(span = { GridItemSpan(2) }) {
            RealTimeGraph(currentHistory.toList())
        }
        item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(100.dp)) }
    }
}
