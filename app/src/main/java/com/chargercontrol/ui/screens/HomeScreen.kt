package com.chargercontrol.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Charge Control", fontSize = 18.sp)
                    Switch(
                        checked = enabledState.value,
                        onCheckedChange = { 
                            scope.launch { prefs.setEnabled(it) }
                        }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Limit: ${limitState.value}%", fontSize = 24.sp)
                Slider(
                    value = limitState.value.toFloat(),
                    onValueChange = { 
                        scope.launch { prefs.setLimit(it.toInt()) }
                    },
                    valueRange = 50f..100f,
                    steps = 49
                )
            }
        }

        Text("Manual Control", modifier = Modifier.padding(vertical = 8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { scope.launch { RootUtils.setCharging(false) } },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("STOP CHARGING")
            }
            Button(
                onClick = { scope.launch { RootUtils.setCharging(true) } },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("RESUME CHARGING")
            }
        }
    }
}
