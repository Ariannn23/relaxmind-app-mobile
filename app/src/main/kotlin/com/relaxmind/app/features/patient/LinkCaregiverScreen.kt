package com.relaxmind.app.features.patient

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.ScoreOrange
import java.nio.charset.StandardCharsets

@Composable
fun LinkCaregiverScreen(
    viewModel: LinkCaregiverViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onLinked: () -> Unit
) {
    val bindingCode by viewModel.bindingCode.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val linked by viewModel.linked.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.createCode()
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) snackbarHostState.showSnackbar(error.orEmpty())
    }

    LaunchedEffect(linked) {
        if (linked) {
            snackbarHostState.showSnackbar("Cuidador vinculado exitosamente")
            onLinked()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RelaxTopBar(
                title = "Vincular Cuidador",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 26.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Text(
                    text = "Muéstrale este código a tu cuidador para que pueda vincularse contigo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
                    textAlign = TextAlign.Center
                )

                val currentCode = bindingCode
                if (currentCode == null && !isLoading && !error.isNullOrBlank()) {
                    LinkErrorState(onRetry = { viewModel.createCode(force = true) })
                }

                currentCode?.let { code ->
                    val qrBitmap = remember(code.id) {
                        generateQrBitmap(
                            """{"code":"${code.code}","patientId":"${code.patientId}"}"""
                        )
                    }

                    QrCard(qrBitmap = qrBitmap)

                    Text(
                        text = "o comparte el código:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PatientGreen.copy(alpha = 0.08f))
                            .padding(vertical = 26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = code.code.formatBindingCode(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 54.sp,
                                letterSpacing = 0.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    TimerLine(remainingSeconds = remainingSeconds)

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = { viewModel.createCode(force = true) },
                        enabled = remainingSeconds == 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (remainingSeconds == 0) PatientGreen.copy(alpha = 0.1f)
                                else Color(0xFFF0F1F3)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = if (remainingSeconds == 0) PatientGreen else Color.Gray
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Generar nuevo código",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (remainingSeconds == 0) PatientGreen else Color.Gray
                        )
                    }
                }
            }

            if (isLoading) {
                FullScreenLoadingOverlay()
            }
        }
    }
}

@Composable
private fun LinkErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "No se pudo generar el código",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Revisa tu conexión o despliega las reglas de Firestore para bindingCodes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
            textAlign = TextAlign.Center
        )
        RelaxButton(
            text = "Intentar nuevamente",
            onClick = onRetry,
            role = AppRole.PATIENT,
            variant = ButtonVariant.PRIMARY
        )
    }
}

@Composable
private fun QrCard(qrBitmap: Bitmap) {
    Box(
        modifier = Modifier
            .size(310.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, PatientGreen.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "Código QR de vinculación",
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(listOf(PatientGreen, PatientGreen.copy(alpha = 0.68f))),
                    shape = RoundedCornerShape(24.dp)
                )
        )
    }
}

@Composable
private fun TimerLine(remainingSeconds: Int) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val color = if (remainingSeconds <= 60) ScoreOrange else PatientGreen

    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = color
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Este código expira en %02d:%02d".format(minutes, seconds),
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun generateQrBitmap(content: String): Bitmap {
    val hints = mapOf(
        EncodeHintType.CHARACTER_SET to StandardCharsets.UTF_8.name(),
        EncodeHintType.MARGIN to 1
    )
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints)
    val bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.ARGB_8888)
    for (x in 0 until QR_SIZE) {
        for (y in 0 until QR_SIZE) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

private fun String.formatBindingCode(): String {
    return if (length == 6) "${take(3)} ${drop(3)}" else this
}

private const val QR_SIZE = 720
