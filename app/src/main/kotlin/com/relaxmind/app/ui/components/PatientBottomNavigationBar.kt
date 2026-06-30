package com.relaxmind.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.relaxmind.app.R
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen

// Colors for Patient Navbar
val NavBackground = Color(0xFFF8FFFB)
val NavBorder = Color(0xFFDDEFE8)
val NavInactive = Color(0xFF8AA39C)
val TextPrimary = Color(0xFF1F2430)

// Dark mode colors
val DarkNavBackground = Color(0xFF0B211F)
val DarkNavBorder = Color(0xFF244B45)
val DarkNavInactive = Color(0xFF8AA39C)

@Immutable
data class PatientBottomNavItemData(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

val patientNavBarItems = listOf(
    PatientBottomNavItemData("patient/dashboard", R.string.nav_home, RelaxIcons.Home),
    PatientBottomNavItemData("patient/meditate", R.string.nav_meditate, RelaxIcons.Meditation),
    PatientBottomNavItemData("patient/progress", R.string.nav_progress, RelaxIcons.Progress),
    PatientBottomNavItemData("patient/schedule", R.string.nav_schedule, RelaxIcons.Calendar),
    PatientBottomNavItemData("patient/settings", R.string.nav_settings, RelaxIcons.Settings)
)

fun isPatientTabSelected(currentRoute: String?, tabRoute: String): Boolean {
    if (currentRoute == null) return false
    // Handles nested routes. Ex: patient/schedule/create -> returns true for patient/schedule
    return currentRoute == tabRoute || currentRoute.startsWith("$tabRoute/")
}

@Composable
fun PatientBottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    darkMode: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val hideBottomBarRoutes = listOf(
        "patient/sos",
        "patient/meditationDetail",
        "patient/diary-entry",
        "patient/articleDetail",
        "patient/achievement_library"
    )

    val isHidden = currentRoute != null && hideBottomBarRoutes.any { currentRoute.startsWith(it.substringBefore("/{")) }
    
    AnimatedVisibility(
        visible = !isHidden,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val navShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        val bgColor = if (darkMode) DarkNavBackground else NavBackground
        val borderColor = if (darkMode) DarkNavBorder else NavBorder

        Box(
            modifier = Modifier
                .fillMaxWidth()
                // The navbar is attached to the bottom, without side or bottom margins
                .shadow(
                    elevation = 16.dp,
                    shape = navShape,
                    clip = false,
                    ambientColor = PatientGreen.copy(alpha = 0.08f),
                    spotColor = PatientGreen.copy(alpha = 0.08f)
                )
                .background(bgColor, navShape)
                .border(1.dp, borderColor, navShape)
                .navigationBarsPadding()
                .height(84.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                patientNavBarItems.forEach { item ->
                    val isSelected = isPatientTabSelected(currentRoute, item.route)
                    PatientBottomNavItem(
                        item = item,
                        selected = isSelected,
                        darkMode = darkMode,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo("patient/dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PatientBottomNavItem(
    item: PatientBottomNavItemData,
    selected: Boolean,
    darkMode: Boolean,
    onClick: () -> Unit
) {
    val activeGreen = if (darkMode) Color(0xFF68D391) else PatientGreen
    val inactiveColor = if (darkMode) DarkNavInactive else NavInactive
    val gradientColors = if (darkMode) listOf(Color(0xFF68D391), Color(0xFF1FBF8A)) else listOf(Color(0xFF68D391), Color(0xFF0F6E56))

    val circleScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "circle-scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else inactiveColor,
        animationSpec = tween(200),
        label = "icon-color"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) activeGreen else inactiveColor,
        animationSpec = tween(200),
        label = "text-color"
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            if (circleScale > 0f) {
                Box(
                    modifier = Modifier
                        .size(44.dp * circleScale)
                        .background(Brush.linearGradient(gradientColors), CircleShape)
                        .shadow(
                            elevation = 4.dp * circleScale,
                            shape = CircleShape,
                            spotColor = activeGreen.copy(alpha = 0.5f)
                        )
                )
            }
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(id = item.labelRes),
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = stringResource(id = item.labelRes),
            fontFamily = LexendFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 11.sp,
            color = textColor
        )
    }
}
