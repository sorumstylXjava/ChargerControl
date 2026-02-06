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
    val limit by prefs.limitFlow.collectAsState(initial = 100)
    var currentLevel by remember { mutableStateOf(0) }
    var bypassStatus by remember { mutableStateOf("NORMAL") }
    var lastChargingState by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(isEnabled, limit) {
        if (isEnabled) {
            while(true) {
                currentLevel = BatteryControl.getBatteryLevel(context)
                
                val shouldEnableCharging = when {
                    currentLevel >= limit -> false
                    currentLevel <= (limit - 3) -> true
                    else -> lastChargingState ?: true
                }

                if (shouldEnableCharging != lastChargingState) {
                    BatteryControl.setChargingLimit(shouldEnableCharging)
                    lastChargingState = shouldEnableCharging
                }
                
                delay(15000)
            }
        } else {
            if (lastChargingState != true) {
                BatteryControl.setChargingLimit(true)
                lastChargingState = true
                bypassStatus = "NORMAL"
            }
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
                    Text(if (isEnabled) "ACTIVE" else "INACTIVE", color = if (isEnabled) Color(0xFF00E676) else Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        scope.launch { 
                            prefs.setEnabled(it)
                        } 
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled) Color(0xFF121212) else Color(0xFF121212).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Charging Limit", 
                    color = if (isEnabled) Color.White else Color.Gray, 
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = limit.toFloat(),
                    onValueChange = { scope.launch { prefs.setLimit(it.toInt()) } },
                    enabled = isEnabled,
                    valueRange = 50f..100f,
                    steps = 50,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isEnabled) Color(0xFF00E676) else Color.DarkGray,
                        activeTrackColor = if (isEnabled) Color(0xFF00E676) else Color.DarkGray
                    )
                )
                Text(
                    if (isEnabled) "Batas: $limit% (Akan mengisi lagi di ${limit - 3}%)" else "Aktifkan service untuk mengatur limit", 
                    color = Color.Gray, 
                    fontSize = 11.sp
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
                Text("Manual Bypass", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Status: $bypassStatus", color = Color.Gray, fontSize = 12.sp)
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isEnabled) {
                                bypassStatus = "BYPASS ACTIVE"
                                lastChargingState = false
                                BatteryControl.setChargingLimit(false)
                                Toast.makeText(context, "Bypass ON", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Aktifkan Service Dahulu", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B5E20),
                            contentColor = Color.White
                        )
                    ) {
                        Text("ON", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            bypassStatus = "NORMAL"
                            lastChargingState = true
                            BatteryControl.setChargingLimit(true)
                            Toast.makeText(context, "Bypass OFF", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB71C1C),
                            contentColor = Color.White
                        )
                    ) {
                        Text("OFF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
