package com.chargercontrol.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
    var isBypassActive by remember { mutableStateOf(false) }
    
    // Memberikan tipe data Boolean eksplisit agar Kotlin tidak bingung (Fix Error T)
    val isRooted = remember { mutableStateOf<Boolean>(BatteryControl.checkRoot()) }

    val engineColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFF00E676) else Color(0xFFE53935),
        label = "engineColor"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF080808)).padding(24.dp)
    ) {
        // --- HEADER: RDX8 ENGINE ---
        Surface(
            modifier = Modifier.fillMaxWidth().height(110.dp),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(32.dp),
            // Fix Error: Menggunakan BorderStroke, bukan AssistChipBorder
            border = BorderStroke(1.dp, engineColor.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("RDX8 ENGINE", color = engineColor, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text(if (isEnabled) "STATUS: ACTIVE" else "STATUS: STOPPED", color = Color.Gray, fontSize = 12.sp)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { scope.launch { prefs.saveEnabled(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- BYPASS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Manual Bypass", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isBypassActive = true
                            BatteryControl.setCharging(false)
                            delay(5000)
                            BatteryControl.setCharging(true)
                            isBypassActive = false
                            Toast.makeText(context, "Bypass RDX8 Selesai", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isBypassActive,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text(if (isBypassActive) "PROCESSING..." else "RUN BYPASS (5s)")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- INFO CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                InfoRow("Manufacturer", android.os.Build.MANUFACTURER.uppercase())
                InfoRow("Root Status", if(isRooted.value) "GRANTED" else "DENIED")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
