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
            Row(Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("RDX8 ENGINE", color = if(isEnabled) Color(0xFF00E676) else Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("Status: ${if(isEnabled) "RUNNING" else "STOPPED"}", color = Color.Gray, fontSize = 12.sp)
                }
                Switch(checked = isEnabled, onCheckedChange = { scope.launch { prefs.saveEnabled(it) } })
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
                    Icon(Icons.Rounded.Bolt, null, tint = Color(0xFF00E676))
                    Spacer(Modifier.width(8.dp))
                    Text("Charging Limit", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("$limit%", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Slider(
                    value = limit.toFloat(),
                    onValueChange = { scope.launch { prefs.saveLimit(it.toInt()) } },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00E676), activeTrackColor = Color(0xFF00E676))
                )
                Text("Arus akan diputus otomatis saat baterai mencapai $limit%", color = Color.Gray, fontSize = 11.sp)
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
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isBypassActive = true
                            BatteryControl.setCharging(false) 
                            delay(5000)
                            BatteryControl.setCharging(true)
                            isBypassActive = false
                            Toast.makeText(context, "Bypass RDX8: Arus di-resume!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isBypassActive,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text(if (isBypassActive) "BYPASSING (5s)..." else "AKTIFKAN BYPASS")
                }
            }
        }
    }
}
