package com.chargercontrol.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargercontrol.ui.components.InfoCard
import com.chargercontrol.ui.components.LargeBatteryCard

@Composable
fun StatusScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = Color(0xFF2196F3),
            modifier = Modifier.fillMaxWidth().height(80.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 20.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "Battery Information",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(1) }) {
                LargeBatteryCard("62%", "Discharging")
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard(Icons.Outlined.Schedule, "Time Left", "measuring", color = Color.Red)
                    InfoCard(Icons.Outlined.Bolt, "Voltage", "3835 mV", color = Color.Blue)
                }
            }

            item {
                InfoCard(Icons.Outlined.BatteryStd, "Current Cap", "3100 mAh", "Total 5000 mAh", Color(0xFF9C27B0))
            }
            item {
                InfoCard(Icons.Outlined.Thermostat, "Temperature", "40.3 °C", "Min/Max 40.3°C", Color(0xFFFF9800))
            }

            item {
                InfoCard(Icons.Outlined.Favorite, "Battery Health", "Good", color = Color(0xFFE91E63))
            }
            item {
                InfoCard(Icons.Outlined.ElectricBolt, "Watt", "-1.9 W", color = Color(0xFF00BCD4))
            }

            item(span = { GridItemSpan(2) }) {
                InfoCard(Icons.Outlined.Settings, "Battery Type", "Li-poly", color = Color(0xFF009688))
            }
        }
    }
}
