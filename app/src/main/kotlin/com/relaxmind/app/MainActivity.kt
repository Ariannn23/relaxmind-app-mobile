package com.relaxmind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.ui.themes.ThemeState
import com.relaxmind.app.utils.OnboardingPreferences
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {
    private val authService = FirebaseAuthService()
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channels and schedule workers
        com.relaxmind.app.services.NotificationUtils.createNotificationChannels(this)
        com.relaxmind.app.services.NotificationUtils.scheduleDailyCheckInReminder(this)

        // Read the onboarding flag before composition so the start destination is stable.
        val onboardingSeen = OnboardingPreferences.isSeen(this)

        setContent {
            val darkMode by ThemeState.darkMode.collectAsState()

            RelaxMindTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentUser = authService.getCurrentUser()
                    var isCheckingSession by remember { mutableStateOf(currentUser != null) }
                    var isAuthenticated by remember { mutableStateOf(currentUser != null) }
                    var userRole by remember { mutableStateOf<String?>(null) }
                    var isNewPatient by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        if (currentUser != null) {
                            try {
                                val patient = withTimeoutOrNull(5000L) {
                                    firestoreRepository.getPatientById(currentUser.uid).getOrNull()
                                }
                                if (patient != null) {
                                    ThemeState.darkMode.value = patient.darkMode
                                    ThemeState.language.value = patient.language
                                    updateAppLocale(patient.language)
                                    userRole = "patient"
                                    isNewPatient = !patient.onboardingCompleted
                                } else {
                                    val caregiver = withTimeoutOrNull(5000L) {
                                        firestoreRepository.getCaregiverById(currentUser.uid).getOrNull()
                                    }
                                    if (caregiver != null) {
                                        ThemeState.darkMode.value = caregiver.darkMode
                                        ThemeState.language.value = caregiver.language
                                        updateAppLocale(caregiver.language)
                                        userRole = "caregiver"
                                    } else {
                                        authService.logout()
                                        isAuthenticated = false
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                authService.logout()
                                isAuthenticated = false
                            }
                            isCheckingSession = false
                        }
                    }

                    if (isCheckingSession) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        val navController = rememberNavController()
                        AppNavGraph(
                            navController = navController,
                            startDestination = resolveStartDestination(
                                isAuthenticated = isAuthenticated,
                                role = userRole,
                                isNewPatient = isNewPatient,
                                onboardingSeen = onboardingSeen
                            )
                        )
                        
                        LaunchedEffect(intent) {
                            val action = intent.getStringExtra("action")
                            when (action) {
                                "open_sos" -> {
                                    val alertId = intent.getStringExtra("alertId")
                                    if (alertId != null) {
                                        navController.navigate(com.relaxmind.app.Screen.SOSAlert.createRoute(alertId))
                                    }
                                }
                                "open_patient_detail" -> {
                                    val patientId = intent.getStringExtra("patientId")
                                    if (patientId != null) {
                                        navController.navigate(com.relaxmind.app.Screen.PatientDetail.createRoute(patientId))
                                    }
                                }
                                "open_checkin" -> {
                                    navController.navigate(com.relaxmind.app.Screen.CheckIn.route)
                                }
                                "open_appointment" -> {
                                    val appointmentId = intent.getStringExtra("appointmentId")
                                    if (appointmentId != null) {
                                        navController.navigate(com.relaxmind.app.Screen.AppointmentDetail.createRoute(appointmentId))
                                    }
                                }
                            }
                        }
                        
                        if (isAuthenticated && userRole == "caregiver") {
                            com.relaxmind.app.features.caregiver.GlobalCaregiverAlertObserver(navController = navController)
                        }
                    }
                }
            }
        }
    }

    private fun updateAppLocale(lang: String) {
        try {
            val locale = java.util.Locale(lang)
            java.util.Locale.setDefault(locale)
            val resources = this.resources
            val configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
