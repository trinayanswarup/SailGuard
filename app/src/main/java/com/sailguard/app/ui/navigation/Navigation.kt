package com.sailguard.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Setup     : Screen("setup",     "Setup",     Icons.Filled.FlightTakeoff)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object Smart     : Screen("smart",     "Smart",     Icons.Filled.AutoAwesome)
    object Alerts    : Screen("alerts",    "Alerts",    Icons.Filled.Notifications)
    object History   : Screen("history",   "History",   Icons.Filled.History)
}

val bottomNavScreens = listOf(
    Screen.Setup,
    Screen.Dashboard,
    Screen.Smart,
    Screen.Alerts,
    Screen.History
)
