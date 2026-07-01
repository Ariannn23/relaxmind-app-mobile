package com.relaxmind.app.features.caregiver

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.relaxmind.app.data.model.Patient
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.themes.*
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverLinkPatientScreen(
    viewModel: CaregiverViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onLinked: () -> Unit
) {
    val context = LocalContext.current
    val toastState = rememberRelaxToastState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    val isLinking by viewModel.isLinking.collectAsState()
    val linkedPatientName by viewModel.linkedPatientName.collectAsState()
    val pendingPatientLink by viewModel.pendingPatientLink.collectAsState()

    var manualCode by remember { mutableStateOf("") }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var scanLocked by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            scanLocked = false
            toastState.showError(error.orEmpty())
            viewModel.consumeError()
        }
    }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            showSuccessModal = true
            viewModel.consumeMessage()
        }
    }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = LexendTypography) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Vincularme con paciente",
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
                                role = com.relaxmind.app.ui.components.AppRole.CAREGIVER
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
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. QR Camera Preview Scanner Box (45% height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f)
                            .background(Color.Black)
                    ) {
                        if (hasCameraPermission) {
                            CameraPreview(
                                modifier = Modifier.fillMaxSize(),
                                onCameraError = { cameraError = it },
                                onCodeDetected = { rawValue ->
                                    val code = extractBindingCode(rawValue)
                                    if (!scanLocked && code != null) {
                                        scanLocked = true
                                        viewModel.previewBindingCode(code)
                                    }
                                }
                            )
                            ScannerOverlay(modifier = Modifier.fillMaxSize())
                            cameraError?.let { errorMessage ->
                                CameraUnavailableMessage(message = errorMessage)
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Necesitamos permiso de cámara para escanear el QR",
                                    fontFamily = LexendFontFamily,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                com.relaxmind.app.ui.components.RelaxButton(
                                    text = "Permitir cámara",
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    role = com.relaxmind.app.ui.components.AppRole.CAREGIVER
                                )
                            }
                        }
                    }

                    // 2. Manual Code Panel (Bottom sheet container)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.55f)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                                ambientColor = Color(0x0C000000),
                                spotColor = Color(0x0C000000)
                            )
                            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                            .background(Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Drag Handle
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 4.dp)
                                    .background(Color(0xFFE6E8EF), RoundedCornerShape(50))
                            )

                            Text(
                                text = "o ingresa el código manualmente",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = CaregiverIndigo,
                                textAlign = TextAlign.Center
                            )

                            // 6 individual code input boxes using one basic textfield
                            val focusManager = LocalFocusManager.current
                            val focusRequester = remember { FocusRequester() }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { focusRequester.requestFocus() }
                            ) {
                                BasicTextField(
                                    value = manualCode,
                                    onValueChange = {
                                        val clean = it.filter { c -> c.isDigit() }.take(6)
                                        manualCode = clean
                                        if (clean.length == 6) {
                                            focusManager.clearFocus()
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .alpha(0f)
                                        .size(1.dp)
                                        .focusRequester(focusRequester)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 0 until 6) {
                                        val char = manualCode.getOrNull(i)?.toString() ?: ""
                                        val isFocused = manualCode.length == i || (i == 5 && manualCode.length == 6)
                                        Box(
                                            modifier = Modifier
                                                .size(width = 46.dp, height = 56.dp)
                                                .border(
                                                    width = if (isFocused) 2.dp else 1.dp,
                                                    color = if (isFocused) CaregiverIndigo else Color(0xFFE6E8EF),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .background(Color(0xFFFAF8FF), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char.ifEmpty { "—" },
                                                fontFamily = LexendFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = if (char.isEmpty()) Color.LightGray else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Verify code button
                            com.relaxmind.app.ui.components.RelaxButton(
                                text = "Verificar código",
                                onClick = {
                                    scanLocked = true
                                    viewModel.previewBindingCode(manualCode)
                                },
                                enabled = manualCode.length == 6 && !isLinking,
                                isLoading = isLinking,
                                role = com.relaxmind.app.ui.components.AppRole.CAREGIVER,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Security Info Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFFAF8FF),
                                border = BorderStroke(1.dp, Color(0xFFE5E0F7))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = CaregiverIndigo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "El código tiene 6 dígitos",
                                            fontFamily = LexendFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Es temporal y solo puede usarse una vez.",
                                            fontFamily = LexendFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            // Help instructions
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFEDE9FE), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = CaregiverIndigo,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Pide al paciente que genere un código desde su app RelaxMind",
                                    fontFamily = LexendFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                pendingPatientLink?.let { pending ->
                    ConfirmPatientLinkDialog(
                        patient = pending.patient,
                        isLoading = isLinking,
                        onDismiss = {
                            viewModel.clearPendingPatientLink()
                            scanLocked = false
                        },
                        onConfirm = {
                            viewModel.confirmPendingPatientLink(onSuccess = {})
                        }
                    )
                }

                // Success Modal Dialog
                if (showSuccessModal) {
                    Dialog(onDismissRequest = {
                        showSuccessModal = false
                        onLinked()
                    }) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CaregiverIndigo,
                                    modifier = Modifier.size(64.dp)
                                )

                                Text(
                                    text = "Paciente vinculado",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = TextPrimary
                                )

                                Text(
                                    text = "Ahora puedes acompañar a ${linkedPatientName ?: "tu paciente"} desde tu panel de cuidador.",
                                    fontFamily = LexendFontFamily,
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                com.relaxmind.app.ui.components.RelaxButton(
                                    text = "Ver paciente",
                                    onClick = {
                                        showSuccessModal = false
                                        onLinked()
                                    },
                                    role = com.relaxmind.app.ui.components.AppRole.CAREGIVER,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                if (isLinking) {
                    FullScreenLoadingOverlay()
                }

                RelaxToastHost(state = toastState)
            }
        }
    }
}

@Composable
private fun ConfirmPatientLinkDialog(
    patient: Patient,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val fullName = "${patient.name} ${patient.lastName}".trim().ifBlank { "Paciente RelaxMind" }
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            tonalElevation = 0.dp,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFEDE9FE), Color(0xFFDCD6FF))
                            ),
                            shape = CircleShape
                        )
                        .border(2.dp, CaregiverIndigo.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fullName.initials(),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = CaregiverIndigo
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Confirmar vinculacion",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Antes de enlazarte, verifica que estos datos correspondan al paciente.",
                    fontFamily = LexendFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFFAF8FF),
                    border = BorderStroke(1.dp, Color(0xFFE5E0F7))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PatientPreviewRow(Icons.Default.Person, "Nombre", fullName)
                        if (patient.email.isNotBlank()) {
                            PatientPreviewRow(Icons.Default.Email, "Correo", patient.email)
                        }
                        if (patient.phone.isNotBlank()) {
                            PatientPreviewRow(Icons.Default.Phone, "Telefono", patient.phone)
                        }
                        if (patient.condition.isNotBlank()) {
                            PatientPreviewRow(Icons.Default.MedicalInformation, "Condicion", patient.condition)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.3.dp, Color(0xFFE5E0F7)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CaregiverIndigo)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = CaregiverIndigo)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Si, vincular",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientPreviewRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color(0xFFEDE9FE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CaregiverIndigo,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = LexendFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 2
            )
        }
    }
}

private fun String.initials(): String {
    val parts = trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "RM" }
}

@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val lineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lineOffset"
    )

    Canvas(modifier = modifier) {
        drawRect(Color.Black.copy(alpha = 0.52f))
        val boxWidth = size.width * 0.64f
        val boxHeight = boxWidth
        val left = (size.width - boxWidth) / 2f
        val top = size.height * 0.14f
        val corner = 64f
        val stroke = 8f

        // Transparent viewport box
        drawRoundRect(
            color = Color.White.copy(alpha = 0.12f),
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(width = 2f)
        )

        // Corner outlines
        val corners = listOf(
            Offset(left, top),
            Offset(left + boxWidth, top),
            Offset(left, top + boxHeight),
            Offset(left + boxWidth, top + boxHeight)
        )
        corners.forEachIndexed { index, point ->
            val horizontalDirection = if (index % 2 == 0) 1f else -1f
            val verticalDirection = if (index < 2) 1f else -1f
            drawLine(
                color = Color.White,
                start = point,
                end = Offset(point.x + corner * horizontalDirection, point.y),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = point,
                end = Offset(point.x, point.y + corner * verticalDirection),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }

        // Animated scan line
        val lineY = top + (boxHeight * lineOffset)
        drawLine(
            color = Color(0xFF22C55E), // Vibrant green scanner line
            start = Offset(left + 12f, lineY),
            end = Offset(left + boxWidth - 12f, lineY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun CameraUnavailableMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.94f),
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = CaregiverIndigo,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = message,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onCameraError: (String) -> Unit,
    onCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    AndroidView(
        modifier = modifier,
        factory = { androidContext ->
            val previewView = PreviewView(androidContext)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(androidContext)

            cameraProviderFuture.addListener(
                {
                    runCatching {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val analyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor) { imageProxy ->
                                    analyzeBarcode(imageProxy, onCodeDetected)
                                }
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analyzer
                        )
                    }.onFailure {
                        onCameraError("No se pudo abrir la cámara. Puedes ingresar el código manualmente.")
                    }
                },
                ContextCompat.getMainExecutor(context)
            )

            previewView
        }
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun analyzeBarcode(
    imageProxy: ImageProxy,
    onCodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    BarcodeScanning.getClient()
        .process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull { it.valueType == Barcode.TYPE_TEXT || !it.rawValue.isNullOrBlank() }
                ?.rawValue
                ?.let(onCodeDetected)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private fun extractBindingCode(rawValue: String): String? {
    val jsonCode = Regex(""""code"\s*:\s*"(\d{6})"""").find(rawValue)?.groupValues?.getOrNull(1)
    if (jsonCode != null) return jsonCode
    return rawValue.filter { it.isDigit() }.takeIf { it.length == 6 }
}
