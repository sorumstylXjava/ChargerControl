package com.chargercontrol.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargercontrol.utils.BatteryControl

@Composable
fun HomeScreen() {
    var limit by remember { mutableFloatStateOf(80f) }
    var isApplied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Charging Limit", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
               progress = limit / 100f, 
               modifier = Modifier.size(200.dp),
               color = if (isApplied) Color(0xFF00E676) else Color(0xFF2196F3),
               strokeWidth = 12.dp,
               trackColor = Color(0xFF252525)
               )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${limit.toInt()}%", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                if (isApplied) Text("Active", color = Color(0xFF00E676), fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(50.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Set Battery Stop Level", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Slider(
                    value = limit,
                    onValueChange = { 
                        limit = it 
                        isApplied = false 
                    },
                    valueRange = 50f..100f,
                    steps = 49,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { 
                        BatteryControl.setChargingLimit(limit.toInt())
                        isApplied = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Limit", color = Color.White)
                }
            }
        }
    }
}
