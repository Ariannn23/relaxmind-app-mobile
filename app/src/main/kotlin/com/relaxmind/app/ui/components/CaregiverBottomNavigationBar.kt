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
import com.relaxmind.app.ui.themes.CaregiverPurple

// Colors for Caregiver Navbar
val CaregiverNavBackground = Color(0xFFFAF8FF)
val CaregiverNavBorder = Color(0xFFE5E0F7)
val CaregiverNavInactive = Color(0xFF9086B8)

// Dark mode colors
val DarkCaregiverNavBackground = Color(0xFF130D26)
val DarkCaregiverNavBorder = Color(0xFF2E2459)
val DarkCaregiverNavInactive = Color(0xFF6B609E)

@Immutable
data class CaregiverBottomNavItemData(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

val caregiverNavBarItems = listOf(
    CaregiverBottomNavItemData("caregiver/dashboard", R.string.nav_home, RelaxIcons.Home),
    CaregiverBottomNavItemData("caregiver/patients", R.string.nav_patients, RelaxIcons.Groups),
    CaregiverBottomNavItemData("caregiver/alerts", R.string.nav_alerts, RelaxIcons.Notifications),
    CaregiverBottomNavItemData("caregiver/settings", R.string.nav_settings, RelaxIcons.Settings)
)

fun isCaregiverTabSelected(currentRoute: String?, tabRoute: String): Boolean {
    if (currentRoute == null) return false
    return currentRoute == tabRoute || currentRoute.startsWith("$tabRoute/")
}

@Composable
fun CaregiverBottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    darkMode: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val hideBottomBarRoutes = listOf(
        "caregiver/patient_detail"
    )

    val isHidden = currentRoute != null && hideBottomBarRoutes.any { currentRoute.startsWith(it.substringBefore("/{")) }
    
    AnimatedVisibility(
        visible = !isHidden,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val navShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        val bgColor = if (darkMode) DarkCaregiverNavBackground else CaregiverNavBackground
        val borderColor = if (darkMode) DarkCaregiverNavBorder else CaregiverNavBorder

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = navShape,
                    clip = false,
                    ambientColor = CaregiverPurple.copy(alpha = 0.08f),
                    spotColor = CaregiverPurple.copy(alpha = 0.08f)
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
                caregiverNavBarItems.forEach { item ->
                    val isSelected = isCaregiverTabSelected(currentRoute, item.route)
                    CaregiverBottomNavItem(
                        item = item,
                        selected = isSelected,
                        darkMode = darkMode,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo("caregiver/dashboard") { saveState = true }
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
fun CaregiverBottomNavItem(
    item: CaregiverBottomNavItemData,
    selected: Boolean,
    darkMode: Boolean,
    onClick: () -> Unit
) {
    val activePurple = if (darkMode) Color(0xFF7C3AED) else CaregiverPurple
    val inactiveColor = if (darkMode) DarkCaregiverNavInactive else CaregiverNavInactive
    val gradientColors = if (darkMode) listOf(Color(0xFF7C3AED), Color(0xFF5B21B6)) else listOf(Color(0xFF7C3AED), CaregiverPurple)

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
        targetValue = if (selected) activePurple else inactiveColor,
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
                            spotColor = activePurple.copy(alpha = 0.5f)
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
