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
import com.chargercontrol.utils.RootUtils
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(context) }
    val limitState = prefs.limitFlow.collectAsState(initial = 80)
    val enabledState = prefs.enabledFlow.collectAsState(initial = false)

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("RDX8 Ad Placeholder", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
    horizontalArrangement = Arrangement.SpaceEvenly
) {
    Button(
        onClick = { BatteryControl.stopCharging() }, 
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("STOP CHARGE", color = Color.White)
    }

    Button(
        onClick = { BatteryControl.resumeCharging() }, 
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("RESUME", color = Color.White)
    }
}
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InfoRow("Status", if (enabledState.value) "Running" else "Not Running")
                InfoRow("Battery Limit", "70 - ${limitState.value}")
                InfoRow("Temperature Limit", "Coming soon", Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Battery Limit", modifier = Modifier.align(Alignment.Start), color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    LimitDisplay("70", "Enable at")
                    LimitDisplay(limitState.value.toString(), "Disable at")
                }
                Slider(
                    value = limitState.value.toFloat(),
                    onValueChange = { scope.launch { prefs.setLimit(it.toInt()) } },
                    onValueChangeFinished = {
                        Toast.makeText(context, "Limit set to ${limitState.value}%", Toast.LENGTH_SHORT).show()
                    },
                    valueRange = 70f..100f,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { 
                    scope.launch { 
                        val success = RootUtils.setCharging(false)
                        if (success) Toast.makeText(context, "Charging Stopped 🛑", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(context, "Failed to stop charging", Toast.LENGTH_SHORT).show()
                    } 
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                shape = RoundedCornerShape(28.dp)
            ) { Text("STOP CHARGING", color = Color(0xFF4A148C)) }
            
            Button(
                onClick = { 
                    scope.launch { 
                        val success = RootUtils.setCharging(true)
                        if (success) Toast.makeText(context, "Charging Resumed ⚡", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(context, "Failed to resume charging", Toast.LENGTH_SHORT).show()
                    } 
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(28.dp)
            ) { Text("RESUME CHARGING", color = Color.White) }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LimitDisplay(value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(sub, fontSize = 12.sp, color = Color.Gray)
    }
}
