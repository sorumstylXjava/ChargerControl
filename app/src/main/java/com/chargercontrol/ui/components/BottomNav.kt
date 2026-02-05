package com.chargercontrol.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.* 
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun FloatingNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp) 
            .height(80.dp), 
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF252525), 
            shape = RoundedCornerShape(40.dp), 
            shadowElevation = 10.dp,
            modifier = Modifier
                .width(280.dp) 
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIconItem(Icons.Rounded.Home, "home", currentRoute) { navController.navigate("home") }
                NavIconItem(Icons.Rounded.ElectricBolt, "status", currentRoute) { navController.navigate("status") }
                NavIconItem(Icons.Rounded.Settings, "settings", currentRoute) { navController.navigate("settings") }
            }
        }
    }
}

@Composable
fun NavIconItem(icon: ImageVector, route: String, currentRoute: String?, onClick: () -> Unit) {
    val isSelected = currentRoute == route
    val selectedColor = Color(0xFF00E676) 
    val unselectedColor = Color.Gray

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) selectedColor else unselectedColor,
            modifier = Modifier.size(28.dp)
        )
    }
}
