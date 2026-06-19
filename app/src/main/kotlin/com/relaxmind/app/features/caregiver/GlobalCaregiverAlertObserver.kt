package com.relaxmind.app.features.caregiver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.relaxmind.app.Screen
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository

@Composable
fun GlobalCaregiverAlertObserver(
    navController: NavController,
    authService: FirebaseAuthService = remember { FirebaseAuthService() },
    firestoreRepository: FirestoreRepository = remember { FirestoreRepository() }
) {
    val caregiverId = authService.getCurrentUser()?.uid ?: return

    var handledAlerts by remember { mutableStateOf(setOf<String>()) }

    DisposableEffect(caregiverId) {
        val listener = firestoreRepository.listenAlertsForCaregiver(
            caregiverId = caregiverId,
            onChange = { alerts ->
                val activeSosAlerts = alerts.filter { !it.resolved && it.type == "sos" }
                val newSosAlerts = activeSosAlerts.filter { !handledAlerts.contains(it.id) }

                if (newSosAlerts.isNotEmpty()) {
                    val alertToShow = newSosAlerts.first()
                    handledAlerts = handledAlerts + alertToShow.id

                    val currentRoute = navController.currentDestination?.route
                    val targetRoute = Screen.SOSAlert.createRoute(alertToShow.id)
                    
                    if (currentRoute != targetRoute && currentRoute?.startsWith("caregiver/sos/") != true) {
                        navController.navigate(targetRoute) {
                            launchSingleTop = true
                        }
                    }
                }
            },
            onError = { }
        )

        onDispose {
            listener.remove()
        }
    }
}
