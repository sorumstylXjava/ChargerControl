package com.chargercontrol.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargercontrol.data.Prefs
import com.chargercontrol.utils.BatteryControl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(context) }
    
    val isEnabled by prefs.enabledFlow.collectAsState(initial = false)
    val limit by prefs.limitFlow.collectAsState(initial = 80)
    var currentLevel by remember { mutableStateOf(0) }
    var bypassStatus by remember { mutableStateOf("NORMAL") }

    LaunchedEffect(Unit) {
        while(true) {
            currentLevel = BatteryControl.getBatteryLevel(context)
            if (isEnabled) {
                if (currentLevel >= limit) {
                    BatteryControl.setChargingLimit(false)
                } else {
                    BatteryControl.setChargingLimit(true)
                }
            }
            delay(5000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF080808)).padding(20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Service Status", color = Color.Gray, fontSize = 12.sp)
                    Text(if (isEnabled) "ACTIVE" else "INACTIVE", color = if (isEnabled) Color(0xFF00E676) else Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        scope.launch { 
                            prefs.setEnabled(it)
                            if (!it) BatteryControl.setChargingLimit(true)
                        } 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Charging Limit", color = Color.White, fontWeight = FontWeight.Bold)
                Slider(
                    value = limit.toFloat(),
                    onValueChange = { scope.launch { prefs.setLimit(it.toInt()) } },
                    valueRange = 50f..100f,
                    steps = 10
                )
                Text("Arus otomatis terputus pada $limit%", color = Color.Gray, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Manual Bypass", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Status: $bypassStatus", color = Color.Gray, fontSize = 12.sp)
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            bypassStatus = "BYPASS ACTIVE"
                            BatteryControl.setChargingLimit(false)
                            Toast.makeText(context, "Bypass ON", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("ON")
                    }

                    Button(
                        onClick = {
                            bypassStatus = "NORMAL"
                            BatteryControl.setChargingLimit(true)
                            Toast.makeText(context, "Bypass OFF", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("OFF")
                    }
                }
            }
        }
    }
}
