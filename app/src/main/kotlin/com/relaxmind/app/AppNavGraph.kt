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
import com.relaxmind.app.features.auth.ForgotPasswordScreen
import com.relaxmind.app.features.caregiver.AlertsHistoryScreen
import com.relaxmind.app.features.caregiver.DashboardCaregiverScreen
import com.relaxmind.app.features.caregiver.PatientDetailScreen
import com.relaxmind.app.features.caregiver.PatientsListScreen
import com.relaxmind.app.features.caregiver.ScanQRScreen
import com.relaxmind.app.features.common.WelcomeScreen
import com.relaxmind.app.features.common.CheckInScreen
import com.relaxmind.app.features.patient.DashboardPatientScreen
import com.relaxmind.app.features.patient.SettingsPatientScreen
import com.relaxmind.app.features.patient.ProgressScreen
import com.relaxmind.app.features.patient.MeditateScreen
import com.relaxmind.app.features.patient.MeditationDetailScreen
import com.relaxmind.app.features.patient.ScheduleScreen
import com.relaxmind.app.features.patient.CreateAppointmentScreen
import com.relaxmind.app.features.patient.AppointmentDetailScreen
import com.relaxmind.app.features.patient.DiaryScreen
import com.relaxmind.app.features.patient.DiaryEntryScreen
import com.relaxmind.app.features.patient.LinkCaregiverScreen
import com.relaxmind.app.features.patient.SOSPatientScreen
import com.relaxmind.app.features.patient.lumi.LumiChatScreen
import com.relaxmind.app.features.patient.lumi.LumiHistoryScreen
import com.relaxmind.app.features.patient.EditProfileScreen
import com.relaxmind.app.features.caregiver.EditProfileCaregiverScreen
import com.relaxmind.app.features.patient.NearbyHealthScreen
import com.relaxmind.app.features.caregiver.SOSAlertScreen

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
    data object Diary : Screen("patient/diary")
    data object DiaryEntry : Screen("patient/diary-entry")
    data object LumiChat : Screen("patient/lumi?sessionId={sessionId}") {
        const val SessionIdArg = "sessionId"
        fun createRoute(sessionId: String? = null): String {
            return if (sessionId != null) "patient/lumi?sessionId=$sessionId" else "patient/lumi"
        }
    }
    data object LumiHistory : Screen("patient/lumi/history")
    data object PatientSettings : Screen("patient/settings")
    data object EditProfile : Screen("patient/profile/edit")
    data object LinkCaregiver : Screen("patient/link-caregiver")
    data object NearbyHealth : Screen("patient/nearby-health")
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
    data object CaregiverEditProfile : Screen("caregiver/profile/edit")
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
                },
                onNavigateToPatientDashboard = {
                    navController.navigate(Screen.PatientDashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToCaregiverDashboard = {
                    navController.navigate(Screen.CaregiverDashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                autoSendCode = false,
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
        composable(Screen.ForgotPassword.route) { 
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }

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
        composable(Screen.Schedule.route) {
            ScheduleScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.CreateAppointment.route) {
            CreateAppointmentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AppointmentDetail.route,
            arguments = listOf(navArgument(Screen.AppointmentDetail.AppointmentIdArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString(Screen.AppointmentDetail.AppointmentIdArg).orEmpty()
            AppointmentDetailScreen(
                appointmentId = appointmentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Diary.route) {
            DiaryScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreateEntry = { navController.navigate(Screen.DiaryEntry.route) }
            )
        }
        composable(Screen.DiaryEntry.route) {
            DiaryEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Screen.Diary.route) {
                        popUpTo(Screen.Diary.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.LumiChat.route,
            arguments = listOf(navArgument(Screen.LumiChat.SessionIdArg) { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString(Screen.LumiChat.SessionIdArg)
            LumiChatScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(Screen.LumiHistory.route) }
            )
        }
        composable(Screen.LumiHistory.route) {
            LumiHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionSelected = { sessionId ->
                    navController.navigate(Screen.LumiChat.createRoute(sessionId))
                }
            )
        }
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
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LinkCaregiver.route) {
            LinkCaregiverScreen(
                onNavigateBack = { navController.popBackStack() },
                onLinked = {
                    navController.navigate(Screen.PatientDashboard.route) {
                        popUpTo(Screen.LinkCaregiver.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.SOSPatient.route) {
            SOSPatientScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.NearbyHealth.route) {
            NearbyHealthScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CaregiverDashboard.route) {
            DashboardCaregiverScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.CaregiverDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onScanQr = { navController.navigate(Screen.ScanQR.route) },
                onPatientClick = { patientId -> navController.navigate(Screen.PatientDetail.createRoute(patientId)) },
                onAlertsClick = { navController.navigate(Screen.AlertsHistory.route) }
            )
        }
        composable(Screen.PatientsList.route) {
            PatientsListScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.CaregiverDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onPatientClick = { patientId -> navController.navigate(Screen.PatientDetail.createRoute(patientId)) },
                onScanQr = { navController.navigate(Screen.ScanQR.route) }
            )
        }
        composable(
            route = Screen.PatientDetail.route,
            arguments = listOf(navArgument(Screen.PatientDetail.PatientIdArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString(Screen.PatientDetail.PatientIdArg).orEmpty()
            PatientDetailScreen(
                patientId = patientId,
                onNavigateBack = { navController.popBackStack() },
                onSosAlertClick = { alertId -> navController.navigate(Screen.SOSAlert.createRoute(alertId)) }
            )
        }
        composable(Screen.AlertsHistory.route) {
            AlertsHistoryScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.CaregiverDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(
            route = Screen.SOSAlert.route,
            arguments = listOf(navArgument(Screen.SOSAlert.AlertIdArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString(Screen.SOSAlert.AlertIdArg).orEmpty()
            SOSAlertScreen(
                alertId = alertId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ScanQR.route) {
            ScanQRScreen(
                onNavigateBack = { navController.popBackStack() },
                onLinked = {
                    navController.navigate(Screen.CaregiverDashboard.route) {
                        popUpTo(Screen.ScanQR.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.CaregiverSettings.route) { 
            com.relaxmind.app.features.caregiver.SettingsCaregiverScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.CaregiverEditProfile.route) },
                onLogout = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigate = { route -> 
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.CaregiverEditProfile.route) {
            EditProfileCaregiverScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
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
