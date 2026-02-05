package com.chargercontrol.ui.screens

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
import com.chargercontrol.utils.BatteryControl

@Composable
fun HomeScreen() {
    val isRooted = remember { mutableStateOf(BatteryControl.checkRoot()) }
    
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080808))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("RDX8 ENGINE ACTIVE", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Power Control", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { BatteryControl.setCharging(false) },
                            modifier = Modifier.weight(1f).height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00)),
                            shape = RoundedCornerShape(20.dp)
                        ) { Text("CUT OFF", fontWeight = FontWeight.Bold) }
                        
                        Button(
                            onClick = { BatteryControl.setCharging(true) },
                            modifier = Modifier.weight(1f).height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                            shape = RoundedCornerShape(20.dp)
                        ) { Text("RESTORE", fontWeight = FontWeight.Bold) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    StatusItem("Brand", android.os.Build.MANUFACTURER.uppercase())
                    StatusItem("Root Access", if(isRooted.value) "PERMITTED" else "DENIED", if(isRooted.value) Color(0xFF00E676) else Color.Red)
                }
            }
        }

        
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Surface(
                color = Color(0xFF222222).copy(alpha = 0.9f),
                shape = RoundedCornerShape(40.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.width(260.dp).height(70.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Home, null, tint = Color.White)
                    Icon(Icons.Rounded.BarChart, null, tint = Color.Gray)
                    Icon(Icons.Rounded.Settings, null, tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, color: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, color = color, fontWeight = FontWeight.ExtraBold)
    }
}
