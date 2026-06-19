package com.relaxmind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.utils.OnboardingPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read the onboarding flag before composition so the start destination is stable.
        val onboardingSeen = OnboardingPreferences.isSeen(this)

        setContent {
            RelaxMindTheme {
                Surface {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        startDestination = resolveStartDestination(
                            isAuthenticated = false,
                            role = null,
                            onboardingSeen = onboardingSeen
                        )
                    )
                }
            }
        }
    }
}
