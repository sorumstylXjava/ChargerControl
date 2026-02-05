package com.chargercontrol.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
    
    // State Management
    val isEnabled by prefs.enabledFlow.collectAsState(initial = false)
    var isBypassActive by remember { mutableStateOf(false) }
    val isRooted = remember { mutableStateOf(BatteryControl.checkRoot()) }

    // Animasi warna berdasarkan status Engine
    val engineColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFF00E676) else Color(0xFFE53935),
        label = "engineColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080808))
            .padding(24.dp)
    ) {
        // --- HEADER: RDX8 ENGINE STATUS ---
        Surface(
            modifier = Modifier.fillMaxWidth().height(110.dp),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(32.dp),
            border = AssistChipDefaults.assistChipBorder(borderColor = engineColor.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isEnabled) "RDX8 ACTIVE" else "RDX8 STOPPED",
                        color = engineColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Text("Automatic Control Mode", color = Color.Gray, fontSize = 12.sp)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { scope.launch { prefs.saveEnabled(it) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0xFF00E676).copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- BYPASS CONTROL CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ElectricBolt, null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Manual Bypass", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Text(
                    "Memutus arus pengisian selama 5 detik untuk kalibrasi sirkuit.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            isBypassActive = true
                            BatteryControl.setCharging(false)
                            Toast.makeText(context, "Bypass: Arus Diputus", Toast.LENGTH_SHORT).show()
                            delay(5000)
                            BatteryControl.setCharging(true)
                            isBypassActive = false
                            Toast.makeText(context, "Bypass: Selesai", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isBypassActive,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBypassActive) Color.DarkGray else Color(0xFF2196F3)
                    )
                ) {
                    if (isBypassActive) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("PROCESSING...")
                    } else {
                        Text("JALANKAN BYPASS (5s)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SYSTEM INFO CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Device & System", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                SystemInfoRow(Icons.Rounded.Smartphone, "Manufacturer", android.os.Build.MANUFACTURER.uppercase())
                SystemInfoRow(Icons.Rounded.Memory, "Model", android.os.Build.MODEL)
                SystemInfoRow(
                    Icons.Rounded.Shield, 
                    "Root Access", 
                    if(isRooted.value) "GRANTED" else "DENIED",
                    if(isRooted.value) Color(0xFF00E676) else Color(0xFFE53935)
                )
            }
        }
    }
}

@Composable
fun SystemInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = Color.Gray, fontSize = 14.sp)
        }
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
