package com.relaxmind.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.relaxmind.app.R
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen

enum class UnlinkDialogType {
    RECEIVED, // When the other party unlinked you
    CONFIRMATION // When you successfully unlinked them
}

@Composable
fun UnlinkNotificationDialog(
    type: UnlinkDialogType,
    otherPartyName: String,
    primaryColor: Color,
    iconResId: Int? = null,
    onDismissRequest: () -> Unit
) {
    val defaultImageRes = if (primaryColor == PatientGreen) {
        R.drawable.desvincular_vista_paciente
    } else {
        R.drawable.desvincular_vista_cuidador
    }
    val imageToShow = iconResId ?: defaultImageRes

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Sad Illustration
                Image(
                    painter = painterResource(id = imageToShow),
                    contentDescription = "Desvinculado",
                    modifier = Modifier.size(110.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                val title = if (type == UnlinkDialogType.RECEIVED) {
                    "Vínculo Terminado"
                } else {
                    "Desvinculación Exitosa"
                }

                Text(
                    text = title,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                val message = if (type == UnlinkDialogType.RECEIVED) {
                    "$otherPartyName ha decidido desvincular su cuenta de la tuya. Ya no compartirán información."
                } else {
                    "Te has desvinculado de $otherPartyName correctamente. Ya no compartirán información."
                }

                Text(
                    text = message,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Button
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Entendido",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
