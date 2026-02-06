package com.chargercontrol.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargercontrol.R
import com.chargercontrol.data.Prefs
import com.chargercontrol.utils.BatteryControl
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(context) }
    
    var maxTemp by remember { mutableStateOf(40f) } 
    var autoCutoff by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var isAmoledMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isAmoledMode) Color.Black else Color(0xFF121212))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Settings", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            IconButton(onClick = { isAmoledMode = !isAmoledMode }) {
                Icon(if (isAmoledMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, null, tint = Color.White)
            }
        }

        Text("PROTEKSI & KEAMANAN", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFFFF5252).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Thermostat, null, tint = Color(0xFFFF5252))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Thermal Cut-off", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Stop charge jika overheat", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    Switch(
                        checked = autoCutoff,
                        onCheckedChange = { autoCutoff = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                    )
                }
                
                if (autoCutoff) {
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Batas Suhu", color = Color.Gray, fontSize = 12.sp)
                        Text("${maxTemp.toInt()}°C", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = maxTemp,
                        onValueChange = { maxTemp = it },
                        valueRange = 35f..50f,
                        steps = 14,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5252), activeTrackColor = Color(0xFFFF5252))
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("ROOT & SYSTEM", color = Color(0xFF29B6F6), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))

        SettingsTile(
            icon = Icons.Rounded.Build,
            title = "Kalibrasi Baterai",
            subtitle = "Hapus batterystats.bin (Perlu Reboot)",
            iconColor = Color(0xFFFFA726)
        ) {
            BatteryControl.executeRoot("rm -f /data/system/batterystats.bin")
            Toast.makeText(context, "Stats file deleted. Reboot now.", Toast.LENGTH_LONG).show()
        }

        Spacer(Modifier.height(8.dp))

        SettingsTile(
            icon = Icons.Rounded.Refresh,
            title = "Restart Daemon",
            subtitle = "Muat ulang service charging",
            iconColor = Color(0xFFAB47BC)
        ) {
            BatteryControl.executeRoot("pkill -f com.chargercontrol")
            Toast.makeText(context, "Service daemon restarted", Toast.LENGTH_SHORT).show()
        }

        Spacer(Modifier.height(8.dp))

        SettingsTile(
            icon = Icons.Rounded.Terminal,
            title = "Shell Access",
            subtitle = "Paksa izin tulis sysfs (chmod 777)",
            iconColor = Color.White
        ) {
            BatteryControl.executeRoot("chmod 777 /sys/class/power_supply/battery/*")
            Toast.makeText(context, "Permissions granted", Toast.LENGTH_SHORT).show()
        }

        Spacer(Modifier.height(24.dp))

        Text("INFORMASI", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))

        SettingsTile(
            icon = Icons.Rounded.Info,
            title = "Developer Info",
            subtitle = "Dibuat oleh Java_nih_deks",
            iconColor = Color.White
        ) {
            showInfo = true
        }
        
        Spacer(Modifier.height(50.dp))
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            containerColor = Color(0xFF1E1E1E),
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Tutup", color = Color(0xFF2196F3)) } },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.jawa), 
                        contentDescription = null,
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Java_nih_deks", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Battery Control v2.0 (Ultimate)", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Java_nih_deks"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Telegram Chat")
                    }
                }
            }
        )
    }
}

@Composable
fun SettingsTile(icon: ImageVector, title: String, subtitle: String, iconColor: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = Color.Gray)
        }
    }
}
