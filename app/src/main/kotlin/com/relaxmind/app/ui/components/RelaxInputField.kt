package com.relaxmind.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.ui.themes.SOSCoral

@Composable
fun RelaxInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = label) },
            isError = isError,
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(imageVector = icon, contentDescription = null) }
            },
            trailingIcon = trailingIcon,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PatientGreen,
                focusedLabelColor = PatientGreen,
                cursorColor = PatientGreen,
                errorBorderColor = SOSCoral,
                errorLabelColor = SOSCoral,
                errorCursorColor = SOSCoral
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                color = SOSCoral,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(name = "RelaxInputField Light", showBackground = true)
@Composable
private fun RelaxInputFieldLightPreview() {
    RelaxMindTheme(darkTheme = false) {
        RelaxInputField(
            value = "paciente@mail.com",
            onValueChange = {},
            label = "Correo",
            leadingIcon = Icons.Filled.Email
        )
    }
}

@Preview(name = "RelaxInputField Dark", showBackground = true)
@Composable
private fun RelaxInputFieldDarkPreview() {
    RelaxMindTheme(darkTheme = true) {
        RelaxInputField(
            value = "correo",
            onValueChange = {},
            label = "Correo",
            isError = true,
            errorMessage = "Ingresa un correo valido",
            leadingIcon = Icons.Filled.Email
        )
    }
}
