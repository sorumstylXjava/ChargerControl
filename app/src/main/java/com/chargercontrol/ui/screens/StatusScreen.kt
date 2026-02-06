package com.chargercontrol.ui.screens

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
    
    var level by remember { mutableIntStateOf(0) }
    var temp by remember { mutableFloatStateOf(0f) }
    var volt by remember { mutableIntStateOf(0) }
    var health by remember { mutableStateOf("Good") }
    var tech by remember { mutableStateOf("Li-ion") }
    var currentMA by remember { mutableFloatStateOf(0f) }
    var wattage by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf("Unknown") }
    var capacity by remember { mutableStateOf("N/A") }
    var cycleCount by remember { mutableStateOf("N/A") }
    
    val currentHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(Unit) {
        while(true) {
            level = BatteryControl.getBatteryLevel(context)
            temp = BatteryControl.getTemperature(context)
            volt = BatteryControl.getVoltage(context)
            tech = BatteryControl.getBatteryTechnology(context)
            health = BatteryControl.getBatteryHealth(context)
            status = BatteryControl.getBatteryStatus(context)
            
            currentMA = BatteryControl.getFormattedCurrent(context)
            wattage = BatteryControl.getWattage(context)
            
            capacity = BatteryControl.getDesignedCapacity(context)
            cycleCount = BatteryControl.getCycleCount()

            if (currentHistory.size >= 30) {
                currentHistory.removeAt(0)
            }
            currentHistory.add(currentMA)

            delay(1000) 
        }
    }

    val isCharging = status == "Charging" || status == "Full"
    val wattColor = if (wattage < 0) Color(0xFFFF5252) else Color(0xFF00E676)

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
                            text = "${String.format("%.2f", wattage)} Watt", 
                            color = wattColor, 
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
        item { StatusCard(Icons.Rounded.Power, "Sumber", status, "", Color.White) }
        item { StatusCard(Icons.Rounded.Straighten, "Kapasitas", capacity, "", Color(0xFFFFA500)) }
        item { StatusCard(Icons.Rounded.History, "Cycle Count", cycleCount, "", Color.LightGray) }
        
        item(span = { GridItemSpan(2) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = 100.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Live Current (mA)", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    RealTimeGraph(currentHistory.toList()) 
                }
            }
        }
    }
}
