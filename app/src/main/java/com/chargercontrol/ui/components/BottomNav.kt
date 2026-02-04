package com.chargercontrol.fas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
            .padding(horizontal = 30.dp, vertical = 24.dp)
    ) {
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(50),
            shadowElevation = 12.dp,
            modifier = Modifier.height(65.dp).fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIconItem(Icons.Default.Home, "home", currentRoute) { navController.navigate("home") }
                NavIconItem(Icons.Default.Dashboard, "status", currentRoute) { navController.navigate("status") }
                NavIconItem(Icons.Default.Settings, "settings", currentRoute) { navController.navigate("settings") }
            }
        }
    }
}

@Composable
fun NavIconItem(icon: ImageVector, route: String, currentRoute: String?, onClick: () -> Unit) {
    val isSelected = currentRoute == route
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF2196F3) else Color.Gray,
            modifier = Modifier.size(if (isSelected) 30.dp else 26.dp)
        )
    }
}
