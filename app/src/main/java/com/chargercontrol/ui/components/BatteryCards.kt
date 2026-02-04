package com.chargercontrol.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String = "",
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (subValue.isNotEmpty()) {
                Text(subValue, color = color, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun LargeBatteryCard(percentage: String, status: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Visualisasi Baterai Hijau
            Box(
                modifier = Modifier
                    .size(80.dp, 120.dp)
                    .background(Color(0xFF252525), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.62f) // Contoh 62%
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFF00E676), RoundedCornerShape(4.dp))
                )
                Text(
                    percentage,
                    Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(status, color = Color.Gray, fontSize = 14.sp)
        }
    }
}
