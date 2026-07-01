package com.relaxmind.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.android.libraries.places.api.Places
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import android.content.Intent
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.compose.rememberNavController
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.ui.themes.ThemeState
import com.relaxmind.app.ui.components.RelaxLoadingScreen
import com.relaxmind.app.utils.OnboardingPreferences
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.runtime.CompositionLocalProvider
import com.relaxmind.app.ui.components.toast.LocalRelaxToast
import com.relaxmind.app.ui.components.toast.RelaxToastHost
import com.relaxmind.app.ui.components.toast.RelaxToastHostState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    private val authService = FirebaseAuthService()
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channels
        com.relaxmind.app.services.NotificationUtils.createNotificationChannels(this)

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }

        // Read the onboarding flag before composition so the start destination is stable.
        val onboardingSeen = OnboardingPreferences.isSeen(this)

        setContent {
            val darkMode by ThemeState.darkMode.collectAsState()
            val language by ThemeState.language.collectAsState()
            val localizedContext = remember(language) { createLocalizedContext(language) }
            val localizedConfiguration = localizedContext.resources.configuration
            val toastHostState = remember { RelaxToastHostState() }

            LaunchedEffect(language) {
                updateAppLocale(language)
            }

            RelaxMindTheme(darkTheme = darkMode) {
                CompositionLocalProvider(
                    LocalConfiguration provides localizedConfiguration,
                    LocalRelaxToast provides toastHostState
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
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
                            if (!com.relaxmind.app.utils.SecurityPreferences.isKeepLoggedIn(applicationContext)) {
                                authService.logout()
                                isAuthenticated = false
                                isCheckingSession = false
                                return@LaunchedEffect
                            }
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
                                    updateFcmToken("patient")
                                } else {
                                    val caregiver = withTimeoutOrNull(5000L) {
                                        firestoreRepository.getCaregiverById(currentUser.uid).getOrNull()
                                    }
                                    if (caregiver != null) {
                                        ThemeState.darkMode.value = caregiver.darkMode
                                        ThemeState.language.value = caregiver.language
                                        updateAppLocale(caregiver.language)
                                        userRole = "caregiver"
                                        updateFcmToken("caregiver")
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
                        RelaxLoadingScreen()
                    } else {
                        val navController = rememberNavController()
                        AppNavGraph(
                            navController = navController,
                            startDestination = resolveStartDestination(
                                isAuthenticated = isAuthenticated,
                                role = userRole,
                                isNewPatient = isNewPatient,
                                onboardingSeen = onboardingSeen,
                                isBiometricEnabled = com.relaxmind.app.utils.SecurityPreferences.isBiometricEnabled(applicationContext)
                            ),
                            userRole = userRole
                        )
                        
                        
                        LaunchedEffect(intent) {
                            // Wait for the NavController to fully attach its graph
                            while (navController.currentDestination == null) {
                                kotlinx.coroutines.delay(50)
                            }
                            handleIntentAction(intent, navController)
                        }

                        DisposableEffect(Unit) {
                            val listener = Consumer<Intent> { newIntent ->
                                handleIntentAction(newIntent, navController)
                            }
                            addOnNewIntentListener(listener)
                            onDispose {
                                removeOnNewIntentListener(listener)
                            }
                        }
                        
                        if (isAuthenticated && userRole == "caregiver") {
                            com.relaxmind.app.features.caregiver.GlobalCaregiverAlertObserver(navController = navController)
                        }
                    }
                }
                
                RelaxToastHost(hostState = toastHostState)
            }
        }
    }
}
}

    private fun handleIntentAction(intent: Intent, navController: androidx.navigation.NavHostController) {
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

    private fun createLocalizedContext(lang: String): Context {
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val configuration = resources.configuration
        configuration.setLocale(locale)
        return createConfigurationContext(configuration)
    }

    private fun updateFcmToken(role: String) {
        val user = authService.getCurrentUser()
        if (user == null) {
            android.util.Log.e("FCM_TOKEN", "Cannot update token: User is null")
            return
        }
        
        android.util.Log.d("FCM_TOKEN", "Fetching token for role: $role, user: ${user.uid}")
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                android.util.Log.d("FCM_TOKEN", "Token fetch successful: $token")
                if (!token.isNullOrBlank()) {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        val result = firestoreRepository.updateFcmToken(user.uid, role, token)
                        if (result.isSuccess) {
                            android.util.Log.d("FCM_TOKEN", "Successfully updated token in Firestore!")
                        } else {
                            android.util.Log.e("FCM_TOKEN", "Failed to update token in Firestore", result.exceptionOrNull())
                        }
                    }
                }
            } else {
                android.util.Log.e("FCM_TOKEN", "Failed to fetch FCM token", task.exception)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        com.relaxmind.app.utils.SoundPlayerManager.stopAll()
    }
}

