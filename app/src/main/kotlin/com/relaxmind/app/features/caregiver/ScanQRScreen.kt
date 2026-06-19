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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxInputField
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.CaregiverIndigo
import java.util.concurrent.Executors

@Composable
fun ScanQRScreen(
    viewModel: CaregiverViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onLinked: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    val isLinking by viewModel.isLinking.collectAsState()
    var manualCode by remember { mutableStateOf("") }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var scanLocked by remember { mutableStateOf(false) }

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
            snackbarHostState.showSnackbar(error.orEmpty())
            viewModel.consumeError()
        }
    }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message.orEmpty())
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RelaxTopBar(
                title = "Vincularme con paciente",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onCodeDetected = { rawValue ->
                        val code = extractBindingCode(rawValue)
                        if (!scanLocked && code != null) {
                            scanLocked = true
                            viewModel.verifyBindingCode(code, onSuccess = onLinked)
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CaregiverIndigo.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Permite el acceso a la cámara o ingresa el código manualmente.",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            ScannerOverlay(modifier = Modifier.fillMaxSize())

            ManualCodePanel(
                manualCode = manualCode,
                onManualCodeChange = { value ->
                    manualCode = value.filter { it.isDigit() }.take(6)
                },
                onVerify = {
                    viewModel.verifyBindingCode(manualCode, onSuccess = onLinked)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (isLinking) {
                FullScreenLoadingOverlay()
            }
        }
    }
}

@Composable
private fun ManualCodePanel(
    manualCode: String,
    onManualCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 24.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 54.dp, height = 5.dp)
                .background(Color.LightGray, RoundedCornerShape(50))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(
                text = "o ingresa el código manualmente",
                style = MaterialTheme.typography.titleSmall,
                color = CaregiverIndigo,
                fontWeight = FontWeight.Bold
            )
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }
        RelaxInputField(
            value = manualCode,
            onValueChange = onManualCodeChange,
            label = "Código de 6 dígitos",
            role = AppRole.CAREGIVER,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )
        RelaxButton(
            text = "Verificar código",
            onClick = onVerify,
            role = AppRole.CAREGIVER,
            enabled = manualCode.length == 6,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Pide al paciente que genere un código desde su app",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color.Black.copy(alpha = 0.48f))
        val boxWidth = size.width * 0.64f
        val boxHeight = boxWidth
        val left = (size.width - boxWidth) / 2f
        val top = size.height * 0.18f
        val corner = 72f
        val stroke = 8f

        drawRoundRect(
            color = Color.White.copy(alpha = 0.14f),
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(28f, 28f),
            style = Stroke(width = 2f)
        )

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
        drawLine(
            color = CaregiverIndigo.copy(alpha = 0.88f),
            start = Offset(left + 16f, top + boxHeight / 2f),
            end = Offset(left + boxWidth - 16f, top + boxHeight / 2f),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
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
