package com.relaxmind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.relaxmind.app.ui.themes.RelaxMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelaxMindTheme {
                Surface {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        startDestination = resolveStartDestination(
                            isAuthenticated = false,
                            role = null
                        )
                    )
                }
            }
        }
    }
}
