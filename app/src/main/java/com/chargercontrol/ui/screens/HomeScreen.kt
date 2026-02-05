package com.chargercontrol.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(context) }
    val limitState = prefs.limitFlow.collectAsState(initial = 80)
    val enabledState = prefs.enabledFlow.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)) // Black Material 3 background
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- 1. BANNER / AD PLACEHOLDER ---
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("RDX8 PREMIUM UI", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. MAIN CONTROL (XIAOMI, SAMSUNG, INFINIX SUPPORT) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(28.dp) // Lebih bulat sesuai request
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Manual Control", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { 
                            if(BatteryControl.stopCharging()) {
                                Toast.makeText(context, "Charging Stopped 🛑", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Root Needed!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("STOP", fontWeight = FontWeight.ExtraBold)
                    }

                    Button(
                        onClick = { 
                            BatteryControl.resumeCharging()
                            Toast.makeText(context, "Charging Resumed ⚡", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("RESUME", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. BATTERY LIMIT SLIDER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Auto Limit", color = Color.Gray)
                    Switch(
                        checked = enabledState.value,
                        onCheckedChange = { scope.launch { prefs.setEnabled(it) } }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    LimitDisplay("70", "Min")
                    LimitDisplay(limitState.value.toString(), "Target")
                }

                Slider(
                    value = limitState.value.toFloat(),
                    onValueChange = { scope.launch { prefs.setLimit(it.toInt()) } },
                    valueRange = 70f..100f,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. DEVICE INFO ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InfoRow("Device Status", if (enabledState.value) "Active" else "Idle", Color(0xFF00E676))
                InfoRow("Config Path", "Auto-Detecting...", Color.Gray)
                InfoRow("Root Access", "Granted", Color.Cyan)
            }
        }

        // Space biar nggak ketutup Floating Nav di pojok
        Spacer(modifier = Modifier.height(120.dp))
    }
}

// --- FUNGSI HELPER (Hanya boleh ada SATU di bawah sini) ---

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LimitDisplay(value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(sub, fontSize = 12.sp, color = Color.Gray)
    }
}
