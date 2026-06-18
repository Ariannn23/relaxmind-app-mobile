package com.relaxmind.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    enabled: Boolean = true
) {
    val roleColor = role.primaryColor()
    val shape = RoundedCornerShape(12.dp)
    val textStyle = MaterialTheme.typography.labelLarge
    val buttonModifier = modifier.defaultMinSize(minHeight = 48.dp)

    when (variant) {
        ButtonVariant.PRIMARY -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = roleColor,
                contentColor = Color.White
            )
        ) {
            Text(text = text, style = textStyle)
        }

        ButtonVariant.OUTLINE -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = shape,
            border = BorderStroke(1.dp, roleColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = roleColor)
        ) {
            Text(text = text, style = textStyle)
        }

        ButtonVariant.DESTRUCTIVE -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = SOSCoral,
                contentColor = Color.White
            )
        ) {
            Text(text = text, style = textStyle)
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
