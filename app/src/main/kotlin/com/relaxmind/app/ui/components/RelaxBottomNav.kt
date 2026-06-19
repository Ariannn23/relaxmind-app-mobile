package com.relaxmind.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    val navShape = if (role == AppRole.CAREGIVER) RoundedCornerShape(32.dp) else RoundedCornerShape(40.dp)
    val navBgColor = if (role == AppRole.CAREGIVER) Color(0xFFFDFBFF) else Color(0xFFF4FAF7)
    val navShadowColor = if (role == AppRole.CAREGIVER) Color(0xFF8A88A6).copy(alpha = 0.35f) else Color(0xFF68D391).copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
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
                .then(
                    if (role == AppRole.PATIENT) {
                        Modifier.border(1.dp, Color(0xFFE2F3EB), navShape)
                    } else Modifier
                )
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                val isSelected = selectedRoute == item.route

                if (role == AppRole.PATIENT) {
                    PatientNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigate(item.route) }
                    )
                } else {
                    NormalNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigate(item.route) },
                        role = role
                    )
                }
            }
        }
    }
}

@Composable
private fun NormalNavItem(
    item: RelaxNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    role: AppRole
) {
    val activeColor = if (role == AppRole.CAREGIVER) Color(0xFF2B6CB0) else PatientGreen
    val inactiveColor = if (role == AppRole.CAREGIVER) Color(0xFF8A88A6) else Color(0xFF8FA89B)
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        label = "nav-item-color"
    )

    Column(
        modifier = Modifier
            .width(56.dp)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            fontFamily = LexendFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 11.sp,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        
        if (role == AppRole.CAREGIVER) {
            // Active indicator dot only for caregiver
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = if (isSelected) activeColor else Color.Transparent,
                        shape = CircleShape
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PatientNavItem(
    item: RelaxNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = PatientGreen
    val inactiveColor = Color(0xFF8FA89B)

    // Animated properties for smooth transition
    val offsetAnimation by animateFloatAsState(
        targetValue = if (isSelected) -14f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "nav-item-offset"
    )

    val circleScaleAnimation by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "nav-item-scale"
    )

    val iconColorAnimation by animateColorAsState(
        targetValue = if (isSelected) Color.White else inactiveColor,
        animationSpec = tween(durationMillis = 300),
        label = "nav-item-icon-color"
    )

    val textColorAnimation by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 300),
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
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (circleScaleAnimation > 0.01f) {
                    // 1. Outer integration circle (matches navbar background color, soft green shadow)
                    Box(
                        modifier = Modifier
                            .offset(y = offsetAnimation.dp)
                            .size(62.dp)
                            .scale(circleScaleAnimation)
                            .shadow(
                                elevation = (4 * circleScaleAnimation).dp,
                                shape = CircleShape,
                                ambientColor = Color(0xFF68D391).copy(alpha = 0.15f),
                                spotColor = Color(0xFF68D391).copy(alpha = 0.15f)
                            )
                            .background(Color(0xFFF4FAF7), CircleShape)
                    )

                    // 2. Inner green gradient circle
                    Box(
                        modifier = Modifier
                            .offset(y = offsetAnimation.dp)
                            .size(52.dp)
                            .scale(circleScaleAnimation)
                            .shadow(
                                elevation = (8 * circleScaleAnimation).dp,
                                shape = CircleShape,
                                ambientColor = PatientGreen.copy(alpha = 0.3f),
                                spotColor = PatientGreen.copy(alpha = 0.3f)
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF68D391), Color(0xFF0F6E56))
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

            Text(
                text = item.label,
                fontFamily = LexendFontFamily,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 11.sp,
                color = textColorAnimation,
                modifier = Modifier.offset(y = (offsetAnimation * 0.5f).dp)
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
