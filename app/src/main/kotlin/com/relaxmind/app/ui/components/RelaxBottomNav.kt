package com.relaxmind.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme

@Composable
fun RelaxBottomNav(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    role: AppRole
) {
    val items = when (role) {
        AppRole.PATIENT -> patientNavItems
        AppRole.CAREGIVER -> caregiverNavItems
    }

    // Dynamic state transition persistence across screen recreations
    val initialRoute = remember {
        val last = BottomNavState.lastSelectedRoute
        if (last.isEmpty() || last == selectedRoute || (last.startsWith("patient") != selectedRoute.startsWith("patient"))) {
            selectedRoute
        } else {
            last
        }
    }
    var animatedSelectedRoute by remember { mutableStateOf(initialRoute) }

    LaunchedEffect(selectedRoute) {
        animatedSelectedRoute = selectedRoute
        BottomNavState.lastSelectedRoute = selectedRoute
    }

    val navShape = RoundedCornerShape(40.dp)
    val navBgColor = if (role == AppRole.CAREGIVER) Color(0xFFFAF8FF) else Color(0xFFF4FAF7)
    val navShadowColor = if (role == AppRole.CAREGIVER) Color(0xFF4338A8).copy(alpha = 0.16f) else Color(0xFF68D391).copy(alpha = 0.15f)
    val navBorderColor = if (role == AppRole.CAREGIVER) Color(0xFFE5E0F7) else Color(0xFFE2F3EB)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 10.dp) // padding top buffer prevents elevated items from being clipped
    ) {
        // 1. Background capsule with shadow and border (drawn behind/before Row content)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = navShape,
                    clip = false,
                    ambientColor = navShadowColor,
                    spotColor = navShadowColor
                )
                .background(navBgColor, navShape)
                .border(1.dp, navBorderColor, navShape)
        )

        // 2. Interactive Row (drawn on top of the background Box and its border)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                val isSelected = animatedSelectedRoute == item.route

                ElevatedNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onNavigate(item.route) },
                    role = role
                )
            }
        }
    }
}

@Composable
private fun ElevatedNavItem(
    item: RelaxNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    role: AppRole
) {
    val activeColor = if (role == AppRole.CAREGIVER) Color(0xFF4338A8) else PatientGreen
    val inactiveColor = if (role == AppRole.CAREGIVER) Color(0xFF8A88A6) else Color(0xFF8FA89B)
    val outerColor = if (role == AppRole.CAREGIVER) Color(0xFFFAF8FF) else Color(0xFFF4FAF7)
    val gradientColors = if (role == AppRole.CAREGIVER) {
        listOf(Color(0xFF6D5DF6), Color(0xFF4338A8))
    } else {
        listOf(Color(0xFF68D391), Color(0xFF0F6E56))
    }

    // Animated bouncy spring for lively elevation movement
    val offsetAnimation by animateFloatAsState(
        targetValue = if (isSelected) -18f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nav-item-offset"
    )

    // Smooth elastic scaling for circles
    val circleScaleAnimation by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav-item-scale"
    )

    val iconColorAnimation by animateColorAsState(
        targetValue = if (isSelected) Color.White else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "nav-item-icon-color"
    )

    val textColorAnimation by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "nav-item-text-color"
    )

    Box(
        modifier = Modifier
            .width(68.dp)
            .fillMaxHeight()
            .zIndex(if (isSelected) 1f else 0f)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(40.dp), // reduced size from 64dp to 40dp to bring text closer to icons
                contentAlignment = Alignment.Center
            ) {
                if (circleScaleAnimation > 0.01f) {
                    // 1. Outer integration circle (matches navbar background color, soft green shadow)
                    Box(
                        modifier = Modifier
                            .offset(y = offsetAnimation.dp)
                            .size(62.dp) // overflows the 40dp container naturally without clipping
                            .scale(circleScaleAnimation)
                            .shadow(
                                elevation = (4 * circleScaleAnimation).dp,
                                shape = CircleShape,
                                ambientColor = activeColor.copy(alpha = 0.15f),
                                spotColor = activeColor.copy(alpha = 0.15f)
                            )
                            .background(outerColor, CircleShape)
                    )

                    // 2. Inner role-colored gradient circle
                    Box(
                        modifier = Modifier
                            .offset(y = offsetAnimation.dp)
                            .size(52.dp)
                            .scale(circleScaleAnimation)
                            .shadow(
                                elevation = (8 * circleScaleAnimation).dp,
                                shape = CircleShape,
                                ambientColor = activeColor.copy(alpha = 0.3f),
                                spotColor = activeColor.copy(alpha = 0.3f)
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = gradientColors
                                ),
                                shape = CircleShape
                            )
                    )
                }

                // Icon (drawn on top of the circle if active, or centered normally if inactive)
                Box(
                    modifier = Modifier
                        .offset(y = offsetAnimation.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = iconColorAnimation,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp)) // tight spacing for premium feel

            Text(
                text = item.label,
                fontFamily = LexendFontFamily,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 11.sp,
                color = textColorAnimation,
                modifier = Modifier.offset(y = (offsetAnimation * 0.45f).dp)
            )
        }
    }
}

@Immutable
private data class RelaxNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val patientNavItems = listOf(
    RelaxNavItem("patient/dashboard", "Inicio", RelaxIcons.Home),
    RelaxNavItem("patient/meditate", "Meditar", RelaxIcons.Meditation),
    RelaxNavItem("patient/progress", "Progreso", RelaxIcons.Progress),
    RelaxNavItem("patient/schedule", "Agenda", RelaxIcons.Calendar),
    RelaxNavItem("patient/settings", "Ajustes", RelaxIcons.Settings)
)

private val caregiverNavItems = listOf(
    RelaxNavItem("caregiver/dashboard", "Inicio", RelaxIcons.Home),
    RelaxNavItem("caregiver/patients", "Pacientes", RelaxIcons.Groups),
    RelaxNavItem("caregiver/alerts", "Alertas", RelaxIcons.Notifications),
    RelaxNavItem("caregiver/settings", "Ajustes", RelaxIcons.Settings)
)

@Preview(name = "RelaxBottomNav Patient Light", showBackground = true)
@Composable
private fun RelaxBottomNavPatientLightPreview() {
    RelaxMindTheme(darkTheme = false) {
        RelaxBottomNav(
            selectedRoute = "patient/dashboard",
            onNavigate = {},
            role = AppRole.PATIENT
        )
    }
}

object BottomNavState {
    var lastSelectedRoute: String = ""
}
