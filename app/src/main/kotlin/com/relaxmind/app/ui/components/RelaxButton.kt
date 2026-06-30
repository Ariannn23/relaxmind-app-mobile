package com.relaxmind.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.themes.LexendFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.ui.themes.SOSCoral

@Composable
fun RelaxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    role: AppRole = AppRole.PATIENT,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    customColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val yOffset by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 4f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "relax-button-y-offset"
    )

    val roleColor = customColor ?: role.primaryColor()
    val gradientBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.25f),
            Color.Transparent
        )
    )
    val shape = RoundedCornerShape(50)
    val textColor = when (variant) {
        ButtonVariant.PRIMARY, ButtonVariant.DESTRUCTIVE -> Color.White
        ButtonVariant.OUTLINE -> roleColor
    }

    val textStyle = MaterialTheme.typography.labelLarge.copy(
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = textColor
    )
    
    val is3D = variant == ButtonVariant.PRIMARY || variant == ButtonVariant.DESTRUCTIVE
    
    val buttonModifier = modifier
        .defaultMinSize(minHeight = 54.dp)
        .offset(y = if (is3D && enabled) yOffset.dp else 0.dp)
        .drawBehind {
            if (is3D && enabled) {
                val edgeHeight = 4.dp.toPx() - yOffset.dp.toPx()
                if (edgeHeight > 0) {
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.12f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, edgeHeight),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
                    )
                }
            }
        }

    when (variant) {
        ButtonVariant.PRIMARY -> Button(
            onClick = onClick,
            modifier = buttonModifier
                .background(roleColor, shape)
                .background(gradientBrush, shape),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            interactionSource = interactionSource
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                if (icon != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = text, style = textStyle)
                    }
                } else {
                    Text(text = text, style = textStyle)
                }
            }
        }

        ButtonVariant.OUTLINE -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = shape,
            border = BorderStroke(1.5.dp, roleColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = roleColor),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            interactionSource = interactionSource
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = roleColor,
                    strokeWidth = 2.dp
                )
            } else {
                if (icon != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = text, style = textStyle)
                    }
                } else {
                    Text(text = text, style = textStyle)
                }
            }
        }

        ButtonVariant.DESTRUCTIVE -> Button(
            onClick = onClick,
            modifier = buttonModifier
                .background(SOSCoral, shape)
                .background(gradientBrush, shape),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            interactionSource = interactionSource
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                if (icon != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = text, style = textStyle)
                    }
                } else {
                    Text(text = text, style = textStyle)
                }
            }
        }
    }
}

@Composable
internal fun AppRole.primaryColor(): Color = when (this) {
    AppRole.PATIENT -> PatientGreen
    AppRole.CAREGIVER -> CaregiverIndigo
}

@Preview(name = "RelaxButton Light", showBackground = true)
@Composable
private fun RelaxButtonLightPreview() {
    RelaxMindTheme(darkTheme = false) {
        RelaxButton(text = "Continuar", onClick = {})
    }
}

@Preview(name = "RelaxButton Dark", showBackground = true)
@Composable
private fun RelaxButtonDarkPreview() {
    RelaxMindTheme(darkTheme = true) {
        RelaxButton(
            text = "Eliminar cuenta",
            onClick = {},
            variant = ButtonVariant.DESTRUCTIVE
        )
    }
}
