package com.relaxmind.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.relaxmind.app.features.auth.AvatarSetupScreen
import com.relaxmind.app.features.auth.EmailVerificationScreen
import com.relaxmind.app.features.auth.ForgotPasswordScreen
import com.relaxmind.app.features.auth.LoginScreen
import com.relaxmind.app.features.auth.NotificationPermissionScreen
import com.relaxmind.app.features.auth.RegisterScreen
import com.relaxmind.app.features.auth.ForgotPasswordScreen
import com.relaxmind.app.features.caregiver.AlertsHistoryScreen
import com.relaxmind.app.features.caregiver.DashboardCaregiverScreen
import com.relaxmind.app.features.caregiver.PatientDetailScreen
import com.relaxmind.app.features.caregiver.PatientsListScreen
import com.relaxmind.app.features.caregiver.CaregiverLinkPatientScreen
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
import com.relaxmind.app.features.patient.PatientLinkCaregiverScreen
import com.relaxmind.app.features.patient.RelaxSoundsScreen
import com.relaxmind.app.features.common.LibraryScreen
import com.relaxmind.app.features.common.ArticleDetailScreen
import com.relaxmind.app.features.common.AccountDeletedGoodbyeScreen
import com.relaxmind.app.features.patient.SOSPatientScreen
import com.relaxmind.app.features.patient.lumi.LumiChatScreen
import com.relaxmind.app.features.patient.lumi.LumiHistoryScreen
import com.relaxmind.app.features.patient.EditProfileScreen
import com.relaxmind.app.features.caregiver.EditProfileCaregiverScreen
import com.relaxmind.app.features.patient.NearbyHealthScreen
import com.relaxmind.app.features.caregiver.SOSAlertScreen
import com.relaxmind.app.features.common.TermsAndConditionsScreen
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.CaregiverAddPatientButton
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.themes.ThemeState

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object EmailVerification : Screen("email-verification")
    data object AvatarSetup : Screen("avatar-setup")
    data object NotificationPermission : Screen("notification-permission")
    data object ForgotPassword : Screen("forgot-password")
    data object BiometricLock : Screen("biometric-lock")
    data object AccountDeletedGoodbye : Screen("account-deleted/{role}") {
        const val RoleArg = "role"
        fun createRoute(role: String): String = "account-deleted/$role"
    }

    data object PatientDashboard : Screen("patient/dashboard")
    data object PatientNotifications : Screen("patient/notifications")
    data object CheckIn : Screen("patient/check-in")
    data object InitialTest : Screen("patient/initial-test")
    data object Meditate : Screen("patient/meditate")
    data object MeditationDetail : Screen("patient/meditation/{exerciseId}") {
        const val ExerciseIdArg = "exerciseId"

        fun createRoute(exerciseId: String): String = "patient/meditation/$exerciseId"
    }
    data object Progress : Screen("patient/progress")
    data object PatientAchievementLibrary : Screen("patient/achievement_library")
    data object Schedule : Screen("patient/schedule")
    data object CreateAppointment : Screen("patient/appointments/create")
    data object AppointmentDetail : Screen("patient/appointments/{appointmentId}") {
        const val AppointmentIdArg = "appointmentId"

        fun createRoute(appointmentId: String): String = "patient/appointments/$appointmentId"
    }
    data object Diary : Screen("patient/diary")
    data object DiaryEntry : Screen("patient/diary-entry")
    data object DiaryDayEntries : Screen("patient/diary/entries/{dateString}") {
        const val DateStringArg = "dateString"
        fun createRoute(dateString: String): String = "patient/diary/entries/$dateString"
    }
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
    data object TermsAndConditions : Screen("common/terms-and-conditions/{role}") {
        const val RoleArg = "role"
        fun createRoute(role: String): String = "common/terms-and-conditions/$role"
    }
    data object RelaxSounds : Screen("patient/relax-sounds")
    data object Library : Screen("common/library/{role}") {
        const val RoleArg = "role"
        fun createRoute(role: String): String = "common/library/$role"
    }
    data object ArticleDetail : Screen("common/article/{articleId}/{role}") {
        const val ArticleIdArg = "articleId"
        const val RoleArg = "role"
        fun createRoute(articleId: String, role: String): String = "common/article/$articleId/$role"
    }
}

fun resolveStartDestination(
    isAuthenticated: Boolean,
    role: String?,
    isNewPatient: Boolean = false,
    onboardingSeen: Boolean = false,
    isBiometricEnabled: Boolean = false
): String = when {
    !isAuthenticated && !onboardingSeen -> Screen.Welcome.route
    !isAuthenticated -> Screen.Login.route
    isAuthenticated && isBiometricEnabled -> Screen.BiometricLock.route
    role == "patient" && isNewPatient -> Screen.InitialTest.route
    role == "patient" -> Screen.PatientDashboard.route
    role == "caregiver" -> Screen.CaregiverDashboard.route
    else -> Screen.Login.route
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    userRole: String? = null
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val bottomNavRole = currentRoute.bottomNavRole()
    val selectedBottomRoute = currentRoute.selectedBottomRoute()
    val darkMode by ThemeState.darkMode.collectAsState()

    fun navigateBottomTab(route: String) {
        if (route == selectedBottomRoute) return

        val rootRoute = when {
            route.startsWith("caregiver/") -> Screen.CaregiverDashboard.route
            else -> Screen.PatientDashboard.route
        }

        navController.navigate(route) {
            popUpTo(rootRoute) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    var unlockedAchievement by remember { mutableStateOf<com.relaxmind.app.data.model.UserAchievement?>(null) }
    
    LaunchedEffect(Unit) {
        com.relaxmind.app.features.patient.AchievementManager.achievementUnlockedEvent.collect { achievement ->
            unlockedAchievement = achievement
        }
    }

    if (unlockedAchievement != null) {
        com.relaxmind.app.features.patient.AchievementUnlockedScreen(
            achievement = unlockedAchievement!!,
            onContinue = { unlockedAchievement = null },
            onNavigateToLibrary = {
                unlockedAchievement = null
                navController.navigate(Screen.PatientAchievementLibrary.route)
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (bottomNavRole == AppRole.PATIENT) {
                com.relaxmind.app.ui.components.PatientBottomNavigationBar(
                    navController = navController,
                    darkMode = darkMode
                )
            } else if (bottomNavRole == AppRole.CAREGIVER) {
                com.relaxmind.app.ui.components.CaregiverBottomNavigationBar(
                    navController = navController,
                    darkMode = darkMode
                )
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.CaregiverDashboard.route) {
                CaregiverAddPatientButton(
                    onClick = { navController.navigate(Screen.ScanQR.route) },
                    modifier = Modifier.padding(bottom = 18.dp, end = 4.dp)
                )
            }
        }
    ) { shellPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier // Removed shellPadding so screens draw behind the navbar
        ) {
        composable(Screen.BiometricLock.route) {
            com.relaxmind.app.features.auth.BiometricLockScreen(
                role = if (userRole == "caregiver") AppRole.CAREGIVER else AppRole.PATIENT,
                onUnlockSuccess = {
                    val dest = if (userRole == "caregiver") Screen.CaregiverDashboard.route else Screen.PatientDashboard.route
                    navController.navigate(dest) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogoutClick = {
                    com.relaxmind.app.data.remote.FirebaseAuthService().logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
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
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.AvatarSetup.route) {
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
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.AvatarSetup.route) {
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
                onContinue = { role ->
                    val nextRoute = if (role == "caregiver") {
                        Screen.NotificationPermission.route
                    } else {
                        Screen.InitialTest.route
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.AvatarSetup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.NotificationPermission.route) {
            NotificationPermissionScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinuePatient = {
                    navController.navigate(Screen.InitialTest.route) {
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
        composable(
            route = Screen.AccountDeletedGoodbye.route,
            arguments = listOf(navArgument(Screen.AccountDeletedGoodbye.RoleArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString(Screen.AccountDeletedGoodbye.RoleArg).orEmpty()
            AccountDeletedGoodbyeScreen(
                role = if (role == "caregiver") AppRole.CAREGIVER else AppRole.PATIENT,
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PatientNotifications.route) {
            com.relaxmind.app.features.patient.PatientNotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInitialTest = { navController.navigate(Screen.InitialTest.route) }
            )
        }

        composable(Screen.PatientDashboard.route) {
            DashboardPatientScreen(
                onNavigateToCheckIn = { navController.navigate(Screen.CheckIn.route) },
                onNavigateToInitialTest = { navController.navigate(Screen.InitialTest.route) },
                onNavigateToMeditate = { 
                    navController.navigate(Screen.Meditate.route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToLinkCaregiver = { navController.navigate(Screen.LinkCaregiver.route) },
                onNavigateToSOS = { navController.navigate(Screen.SOSPatient.route) },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                showBottomNav = false
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
                },
                onNavigateToProgress = {
                    navController.navigate(Screen.Progress.route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
                },
                onNavigateToProgress = {
                    navController.navigate(Screen.Progress.route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Meditate.route) {
            MeditateScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                showBottomNav = false
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
                },
                showBottomNav = false
            )
        }
        composable(Screen.PatientAchievementLibrary.route) {
            com.relaxmind.app.features.patient.AchievementLibraryScreen(
                onNavigateBack = { navController.popBackStack() }
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
                },
                showBottomNav = false
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
                onCreateEntry = { navController.navigate(Screen.DiaryEntry.route) },
                onDayClick = { dateString -> navController.navigate(Screen.DiaryDayEntries.createRoute(dateString)) }
            )
        }
        composable(Screen.DiaryEntry.route) {
            com.relaxmind.app.features.patient.DiaryEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.DiaryDayEntries.route,
            arguments = listOf(navArgument(Screen.DiaryDayEntries.DateStringArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString(Screen.DiaryDayEntries.DateStringArg).orEmpty()
            com.relaxmind.app.features.patient.DiaryDayEntriesScreen(
                dateString = dateString,
                onNavigateBack = { navController.popBackStack() },
                onCreateEntry = { navController.navigate(Screen.DiaryEntry.route) }
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
                onAccountDeleted = {
                    navController.navigate(Screen.AccountDeletedGoodbye.createRoute("patient")) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.PatientDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                showBottomNav = false
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LinkCaregiver.route) {
            PatientLinkCaregiverScreen(
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
                onAlertsClick = { navController.navigate(Screen.AlertsHistory.route) },
                showBottomNav = false
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
                onScanQr = { navController.navigate(Screen.ScanQR.route) },
                showBottomNav = false
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
                onSosAlertClick = { alertId -> navController.navigate(Screen.SOSAlert.createRoute(alertId)) },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.CaregiverDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                showBottomNav = false
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
                },
                showBottomNav = false
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
            CaregiverLinkPatientScreen(
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
                onAccountDeleted = {
                    navController.navigate(Screen.AccountDeletedGoodbye.createRoute("caregiver")) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigate = { route -> 
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                showBottomNav = false
            )
        }
        composable(Screen.CaregiverEditProfile.route) {
            EditProfileCaregiverScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.TermsAndConditions.route,
            arguments = listOf(navArgument(Screen.TermsAndConditions.RoleArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString(Screen.TermsAndConditions.RoleArg) ?: "patient"
            TermsAndConditionsScreen(
                role = role,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.RelaxSounds.route) {
            RelaxSoundsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Library.route,
            arguments = listOf(navArgument(Screen.Library.RoleArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString(Screen.Library.RoleArg) ?: "patient"
            LibraryScreen(
                role = role,
                onNavigateToDetail = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId, role))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument(Screen.ArticleDetail.ArticleIdArg) { type = NavType.StringType },
                navArgument(Screen.ArticleDetail.RoleArg) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString(Screen.ArticleDetail.ArticleIdArg) ?: ""
            val role = backStackEntry.arguments?.getString(Screen.ArticleDetail.RoleArg) ?: "patient"
            ArticleDetailScreen(
                articleId = articleId,
                role = role,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        }
    }
}

private val patientBottomRoutes = setOf(
    Screen.PatientDashboard.route,
    Screen.Meditate.route,
    Screen.Progress.route,
    Screen.Schedule.route,
    Screen.PatientSettings.route
)

private val caregiverBottomRoutes = setOf(
    Screen.CaregiverDashboard.route,
    Screen.PatientsList.route,
    Screen.PatientDetail.route,
    Screen.AlertsHistory.route,
    Screen.CaregiverSettings.route
)

private fun String?.bottomNavRole(): AppRole? = when (this) {
    in patientBottomRoutes -> AppRole.PATIENT
    in caregiverBottomRoutes -> AppRole.CAREGIVER
    else -> null
}

private fun String?.selectedBottomRoute(): String? = when (this) {
    Screen.PatientDetail.route -> Screen.PatientsList.route
    in patientBottomRoutes -> this
    in caregiverBottomRoutes -> this
    else -> null
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
