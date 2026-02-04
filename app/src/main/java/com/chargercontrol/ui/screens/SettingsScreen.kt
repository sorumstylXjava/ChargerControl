package com.chargercontrol.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chargercontrol.R

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { showDialog = true },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text("App Information")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = { TextButton(onClick = { showDialog = false }) { Text("Close") } },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.jawa), 
                        contentDescription = "Profile",
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Developer Name")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Java_diks"))
                        context.startActivity(intent)
                    }) {
                        Text("Contact Telegram")
                    }
                }
            }
        )
    }
}
