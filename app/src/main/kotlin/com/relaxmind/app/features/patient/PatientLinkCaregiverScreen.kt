package com.relaxmind.app.features.patient

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.themes.*
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientLinkCaregiverScreen(
    viewModel: LinkCaregiverViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onLinked: () -> Unit
) {
    val bindingCode by viewModel.bindingCode.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val linked by viewModel.linked.collectAsState()
    val linkedCaregiver by viewModel.linkedCaregiver.collectAsState()
    val toastState = rememberRelaxToastState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.createCode()
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            toastState.showError(error.orEmpty())
        }
    }

    LaunchedEffect(linked) {
        if (linked) {
            toastState.showSuccess("Cuidador vinculado con exito")
        }
    }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = LexendTypography) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Vincular cuidador",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.padding(start = 8.dp)) {
                            com.relaxmind.app.ui.components.RelaxBackButton(
                                onClick = onNavigateBack,
                                role = com.relaxmind.app.ui.components.AppRole.PATIENT
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White)
            ) {
                // Background Soft Gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White, Color(0xFFF4FBF7), Color(0xFFF9FDFC))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {


                    Text(
                        text = "Muestra este código a tu cuidador",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    val currentCode = bindingCode
                    if (currentCode == null && !isLoading && !error.isNullOrBlank()) {
                        LinkErrorState(onRetry = { viewModel.createCode(force = true) })
                    }

                    currentCode?.let { code ->
                        val qrBitmap = remember(code.id) {
                            generateQrBitmap("""{"code":"${code.code}","patientId":"${code.patientId}"}""")
                        }

                        // 2. Premium QR Card
                        Surface(
                            modifier = Modifier
                                .size(280.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = Color(0x0C000000),
                                    spotColor = Color(0x0C000000)
                                ),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Código QR de vinculación",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Text(
                            text = "o comparte el código:",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // 3. Code manual with click to copy
                        Text(
                            text = code.code.formatBindingCode(),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 52.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    clipboardManager.setText(AnnotatedString(code.code))
                                    toastState.showSuccess("Código copiado\nCompártelo con tu cuidador.")
                                }
                        )

                        // 4. Timer
                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        val timerColor = if (remainingSeconds <= 60) DangerRed else WarningOrange

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = timerColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (remainingSeconds > 0) {
                                    "Este código expira en %02d:%02d".format(minutes, seconds)
                                } else {
                                    "Este código ha expirado"
                                },
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = timerColor
                            )
                        }

                        // 5. Generar nuevo código button
                        Button(
                            onClick = { viewModel.createCode(force = true) },
                            enabled = remainingSeconds == 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PatientGreen,
                                disabledContainerColor = Color(0xFFE6E8EF),
                                contentColor = Color.White,
                                disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generar nuevo código",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 6. Security Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF4FBF7),
                        border = BorderStroke(1.dp, Color(0xFFE2F3EB))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = PatientGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Tu seguridad es importante",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Este código es temporal y solo puede usarse una vez para vincular a tu cuidador.",
                                    fontFamily = LexendFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    FullScreenLoadingOverlay()
                }

                if (linked) {
                    PatientLinkedSuccessDialog(
                        caregiverName = "${linkedCaregiver?.name.orEmpty()} ${linkedCaregiver?.lastName.orEmpty()}"
                            .trim()
                            .ifBlank { "tu cuidador" },
                        onContinue = {
                            viewModel.consumeLinkedConfirmation()
                            onLinked()
                        }
                    )
                }

                RelaxToastHost(state = toastState)
            }
        }
    }
}

@Composable
private fun PatientLinkedSuccessDialog(
    caregiverName: String,
    onContinue: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onContinue) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color(0xFFEAF8F1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(46.dp)
                    )
                }
                Text(
                    text = "Cuidador vinculado",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$caregiverName ya puede acompanarte desde su panel de cuidador.",
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                com.relaxmind.app.ui.components.RelaxButton(
                    text = "Ir al inicio",
                    onClick = onContinue,
                    role = AppRole.PATIENT,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LinkLeafDecoration(
    modifier: Modifier = Modifier,
    flipped: Boolean
) {
    Canvas(modifier = modifier) {
        val baseX = if (flipped) size.width * 0.28f else size.width * 0.72f
        val direction = if (flipped) 1f else -1f
        drawLine(
            color = PatientGreen.copy(alpha = 0.18f),
            start = Offset(baseX, size.height * 0.8f),
            end = Offset(baseX + direction * size.width * 0.25f, size.height * 0.22f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawOval(
            color = PatientGreen.copy(alpha = 0.16f),
            topLeft = Offset(baseX + direction * size.width * 0.02f, size.height * 0.22f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.34f, size.height * 0.18f)
        )
        drawOval(
            color = PatientGreen.copy(alpha = 0.12f),
            topLeft = Offset(baseX + direction * size.width * 0.14f, size.height * 0.48f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.16f)
        )
    }
}

@Composable
private fun LinkErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No se pudo generar el código",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = DangerRed
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PatientGreen),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Intentar de nuevo", fontFamily = LexendFontFamily)
        }
    }
}

private fun generateQrBitmap(content: String): Bitmap {
    val hints = mapOf(
        EncodeHintType.CHARACTER_SET to StandardCharsets.UTF_8.name(),
        EncodeHintType.MARGIN to 1
    )
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)
    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    for (x in 0 until 512) {
        for (y in 0 until 512) {
            // Branded PatientGreen color for the QR code matrix
            val color = if (matrix[x, y]) android.graphics.Color.rgb(15, 110, 86) else android.graphics.Color.WHITE
            bitmap.setPixel(x, y, color)
        }
    }
    return bitmap
}

private fun String.formatBindingCode(): String {
    return if (length == 6) "${take(3)} ${drop(3)}" else this
}


