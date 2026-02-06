package com.chargercontrol.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    var isBypassActive by remember { mutableStateOf(false) }

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
                    Text("Charger Control", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(if (isEnabled) "Service is Running" else "Service is Stopped", color = if (isEnabled) Color(0xFF00E676) else Color.Gray, fontSize = 12.sp)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        scope.launch { 
                            prefs.setEnabled(it)
                            BatteryControl.setChargingLimit(limit, it)
                        } 
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.BatteryChargingFull, null, tint = Color(0xFF00E676))
                    Spacer(Modifier.width(8.dp))
                    Text("Limit Charging", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Slider(
                    value = limit.toFloat(),
                    onValueChange = { 
                        scope.launch { 
                            prefs.setLimit(it.toInt())
                            if (isEnabled) {
                                BatteryControl.setChargingLimit(it.toInt(), true)
                            }
                        } 
                    },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00E676), activeTrackColor = Color(0xFF00E676))
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("50%", color = Color.Gray, fontSize = 12.sp)
                    Text("${limit}%", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    Text("100%", color = Color.Gray, fontSize = 12.sp)
                }
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
                Text("Bypass System", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isBypassActive = true
                            BatteryControl.setBypass(true) 
                            delay(5000) 
                            BatteryControl.setBypass(false) 
                            isBypassActive = false
                            Toast.makeText(context, "Bypass Selesai", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isBypassActive,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text(if (isBypassActive) "BYPASSING..." else "Run Bypass")
                }
            }
        }
    }
}
