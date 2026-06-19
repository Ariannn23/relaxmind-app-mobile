package com.relaxmind.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.relaxmind.app.features.auth.AvatarSetupScreen
import com.relaxmind.app.features.auth.EmailVerificationScreen
import com.relaxmind.app.features.auth.LoginScreen
import com.relaxmind.app.features.auth.NotificationPermissionScreen
import com.relaxmind.app.features.auth.RegisterScreen
import com.relaxmind.app.features.common.WelcomeScreen
import com.relaxmind.app.features.common.CheckInScreen
import com.relaxmind.app.features.patient.DashboardPatientScreen
import com.relaxmind.app.features.patient.SettingsPatientScreen
import com.relaxmind.app.features.patient.ProgressScreen
import com.relaxmind.app.features.patient.MeditateScreen
import com.relaxmind.app.features.patient.MeditationDetailScreen

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object EmailVerification : Screen("email-verification")
    data object AvatarSetup : Screen("avatar-setup")
    data object NotificationPermission : Screen("notification-permission")
    data object ForgotPassword : Screen("forgot-password")

    data object PatientDashboard : Screen("patient/dashboard")
    data object CheckIn : Screen("patient/check-in")
    data object InitialTest : Screen("patient/initial-test")
    data object Meditate : Screen("patient/meditate")
    data object MeditationDetail : Screen("patient/meditation/{exerciseId}") {
        const val ExerciseIdArg = "exerciseId"

        fun createRoute(exerciseId: String): String = "patient/meditation/$exerciseId"
    }
    data object Progress : Screen("patient/progress")
    data object Schedule : Screen("patient/schedule")
    data object CreateAppointment : Screen("patient/appointments/create")
    data object AppointmentDetail : Screen("patient/appointments/{appointmentId}") {
        const val AppointmentIdArg = "appointmentId"

        fun createRoute(appointmentId: String): String = "patient/appointments/$appointmentId"
    }
    data object DiaryEntry : Screen("patient/diary-entry")
    data object LumiChat : Screen("patient/lumi")
    data object LumiHistory : Screen("patient/lumi/history")
    data object PatientSettings : Screen("patient/settings")
    data object EditProfile : Screen("patient/profile/edit")
    data object LinkCaregiver : Screen("patient/link-caregiver")
    data object SOSPatient : Screen("patient/sos")

    data object CaregiverDashboard : Screen("caregiver/dashboard")
    data object PatientsList : Screen("caregiver/patients")
    data object PatientDetail : Screen("caregiver/patients/{patientId}") {
        const val PatientIdArg = "patientId"

        fun createRoute(patientId: String): String = "caregiver/patients/$patientId"
    }
    data object AlertsHistory : Screen("caregiver/alerts")
    data object SOSAlert : Screen("caregiver/sos/{alertId}") {
        const val AlertIdArg = "alertId"

        fun createRoute(alertId: String): String = "caregiver/sos/$alertId"
    }
    data object ScanQR : Screen("caregiver/scan-qr")
    data object CaregiverSettings : Screen("caregiver/settings")
}

fun resolveStartDestination(
    isAuthenticated: Boolean,
    role: String?,
    isNewPatient: Boolean = false,
    onboardingSeen: Boolean = false
): String = when {
    !isAuthenticated && !onboardingSeen -> Screen.Welcome.route
    !isAuthenticated -> Screen.Login.route
    role == "patient" && isNewPatient -> Screen.InitialTest.route
    role == "patient" -> Screen.PatientDashboard.route
    role == "caregiver" -> Screen.CaregiverDashboard.route
    else -> Screen.Login.route
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onNavigateToPatientDashboard = {
                    navController.navigate(Screen.PatientDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToCaregiverDashboard = {
                    navController.navigate(Screen.CaregiverDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailVerification = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onVerified = {
                    navController.navigate(Screen.AvatarSetup.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AvatarSetup.route) {
            AvatarSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.NotificationPermission.route) {
                        popUpTo(Screen.AvatarSetup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.NotificationPermission.route) {
            NotificationPermissionScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinuePatient = {
                    navController.navigate(Screen.PatientDashboard.route) {
                        popUpTo(Screen.NotificationPermission.route) { inclusive = true }
                    }
                },
                onContinueCaregiver = {
                    navController.navigate(Screen.CaregiverDashboard.route) {
                        popUpTo(Screen.NotificationPermission.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ForgotPassword.route) { PlaceholderScreen("Pantalla Forgot Password") }

        composable(Screen.PatientDashboard.route) {
            DashboardPatientScreen(
                onNavigateToCheckIn = { navController.navigate(Screen.CheckIn.route) },
                onNavigateToMeditate = { navController.navigate(Screen.Meditate.route) },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToLinkCaregiver = { navController.navigate(Screen.LinkCaregiver.route) },
                onNavigateToSOS = { navController.navigate(Screen.SOSPatient.route) },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.CheckIn.route) {
            CheckInScreen(
                isInitialTest = false,
                onNavigateBack = { navController.popBackStack() },
                onFinished = {
                    navController.navigate(Screen.PatientDashboard.route) {
                        popUpTo(Screen.CheckIn.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.InitialTest.route) {
            CheckInScreen(
                isInitialTest = true,
                onNavigateBack = { navController.popBackStack() },
                onFinished = {
                    navController.navigate(Screen.PatientDashboard.route) {
                        popUpTo(Screen.InitialTest.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Meditate.route) {
            MeditateScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(
            route = Screen.MeditationDetail.route,
            arguments = listOf(navArgument(Screen.MeditationDetail.ExerciseIdArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString(Screen.MeditationDetail.ExerciseIdArg).orEmpty()
            MeditationDetailScreen(
                exerciseId = exerciseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Progress.route) {
            ProgressScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Schedule.route) { PlaceholderScreen("Pantalla Schedule") }
        composable(Screen.CreateAppointment.route) { PlaceholderScreen("Pantalla Create Appointment") }
        composable(
            route = Screen.AppointmentDetail.route,
            arguments = listOf(navArgument(Screen.AppointmentDetail.AppointmentIdArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString(Screen.AppointmentDetail.AppointmentIdArg).orEmpty()
            PlaceholderScreen("Pantalla Appointment Detail: $appointmentId")
        }
        composable(Screen.DiaryEntry.route) { PlaceholderScreen("Pantalla Diary Entry") }
        composable(Screen.LumiChat.route) { PlaceholderScreen("Pantalla Lumi Chat") }
        composable(Screen.LumiHistory.route) { PlaceholderScreen("Pantalla Lumi History") }
        composable(Screen.PatientSettings.route) {
            SettingsPatientScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.EditProfile.route) { PlaceholderScreen("Pantalla Edit Profile") }
        composable(Screen.LinkCaregiver.route) { PlaceholderScreen("Pantalla Link Caregiver") }
        composable(Screen.SOSPatient.route) { PlaceholderScreen("Pantalla SOS Paciente") }

        composable(Screen.CaregiverDashboard.route) { PlaceholderScreen("Pantalla Caregiver Dashboard") }
        composable(Screen.PatientsList.route) { PlaceholderScreen("Pantalla Patients List") }
        composable(
            route = Screen.PatientDetail.route,
            arguments = listOf(navArgument(Screen.PatientDetail.PatientIdArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString(Screen.PatientDetail.PatientIdArg).orEmpty()
            PlaceholderScreen("Pantalla Patient Detail: $patientId")
        }
        composable(Screen.AlertsHistory.route) { PlaceholderScreen("Pantalla Alerts History") }
        composable(
            route = Screen.SOSAlert.route,
            arguments = listOf(navArgument(Screen.SOSAlert.AlertIdArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString(Screen.SOSAlert.AlertIdArg).orEmpty()
            PlaceholderScreen("Pantalla SOS Alert: $alertId")
        }
        composable(Screen.ScanQR.route) { PlaceholderScreen("Pantalla Scan QR") }
        composable(Screen.CaregiverSettings.route) { PlaceholderScreen("Pantalla Caregiver Settings") }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall)
    }
}
