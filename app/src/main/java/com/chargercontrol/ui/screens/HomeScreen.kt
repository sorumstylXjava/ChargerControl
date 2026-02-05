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
import com.chargercontrol.utils.BatteryControl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isBypassActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080808))
            .padding(24.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "RDX8 ENGINE ACTIVE",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Bypass Power",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!isBypassActive) {
                            scope.launch {
                                isBypassActive = true
                                BatteryControl.setChargingEnabled(false)
                                Toast.makeText(context, "Charging CUT. Waiting 5s...", Toast.LENGTH_SHORT).show()
                                
                                delay(5000)
                                
                                BatteryControl.setChargingEnabled(true)
                                Toast.makeText(context, "Resumed. 5s Complete.", Toast.LENGTH_SHORT).show()
                                isBypassActive = false
                            }
                        }
                    },
                    enabled = !isBypassActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBypassActive) Color.Gray else Color(0xFF2196F3)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isBypassActive) "PROCESSING..." else "AKTIFKAN BYPASS")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Brand", color = Color.Gray)
                    Text(android.os.Build.MANUFACTURER.uppercase(), color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Root Access", color = Color.Gray)
                    Text("CHECKED", color = Color(0xFF00E676))
                }
            }
        }
    }
}
