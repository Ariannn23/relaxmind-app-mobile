package com.relaxmind.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.relaxmind.app.ui.themes.RelaxMindTheme

@Composable
fun RelaxBottomNav(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    role: AppRole
) {
    val activeColor = role.primaryColor()
    val items = when (role) {
        AppRole.PATIENT -> patientNavItems
        AppRole.CAREGIVER -> caregiverNavItems
    }

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label, style = MaterialTheme.typography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    indicatorColor = activeColor.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Immutable
private data class RelaxNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val patientNavItems = listOf(
    RelaxNavItem("patient/dashboard", "Dashboard", Icons.Filled.Dashboard),
    RelaxNavItem("patient/meditate", "Meditar", Icons.Filled.SelfImprovement),
    RelaxNavItem("patient/progress", "Progreso", Icons.AutoMirrored.Filled.ShowChart),
    RelaxNavItem("patient/schedule", "Agenda", Icons.Filled.CalendarMonth),
    RelaxNavItem("patient/lumi", "Lumi", Icons.Filled.SmartToy)
)

private val caregiverNavItems = listOf(
    RelaxNavItem("caregiver/dashboard", "Dashboard", Icons.Filled.Dashboard),
    RelaxNavItem("caregiver/patients", "Pacientes", Icons.Filled.Groups),
    RelaxNavItem("caregiver/alerts", "Alertas", Icons.Filled.Notifications),
    RelaxNavItem("caregiver/settings", "Ajustes", Icons.Filled.Settings)
)

@Preview(name = "RelaxBottomNav Patient Light", showBackground = true)
@Composable
private fun RelaxBottomNavPatientLightPreview() {
    RelaxMindTheme(darkTheme = false) {
        RelaxBottomNav(
            selectedRoute = "patient/dashboard",
            onNavigate = {},
            role = AppRole.PATIENT
        )
    }
}

@Preview(name = "RelaxBottomNav Caregiver Dark", showBackground = true)
@Composable
private fun RelaxBottomNavCaregiverDarkPreview() {
    RelaxMindTheme(darkTheme = true) {
        RelaxBottomNav(
            selectedRoute = "caregiver/patients",
            onNavigate = {},
            role = AppRole.CAREGIVER
        )
    }
}
