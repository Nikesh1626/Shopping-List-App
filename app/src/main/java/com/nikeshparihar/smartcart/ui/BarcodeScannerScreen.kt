package com.nikeshparihar.smartcart.ui

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// Design tokens matching "Calm Clinical Premium"
private val ScannerOverlayBg = Color(0xCC000000)
private val AccentGreen = Color(0xFF2E7D32)
private val ScanBoxBorder = Color(0xFF66BB6A)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    scannerMessage: String,
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    if (cameraPermission.status.isGranted) {
        CameraPreviewWithScanner(
            onBarcodeScanned = onBarcodeScanned,
            onClose = onClose,
            scannerMessage = scannerMessage,
        )
    } else {
        // Permission denied or rationale screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (cameraPermission.status.shouldShowRationale)
                        "Camera access is needed to scan barcodes on products"
                    else
                        "Camera permission is required for barcode scanning",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { cameraPermission.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onClose) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewWithScanner(
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    scannerMessage: String,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasScanned by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    DisposableEffect(camera) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val barcodeOptions = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_EAN_13,
                            Barcode.FORMAT_EAN_8,
                            Barcode.FORMAT_UPC_A,
                            Barcode.FORMAT_UPC_E,
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_CODE_128,
                            Barcode.FORMAT_CODE_39
                        )
                        .build()
                    val scanner = BarcodeScanning.getClient(barcodeOptions)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        processImage(imageProxy, scanner) { barcode ->
                            if (!hasScanned) {
                                hasScanned = true
                                onBarcodeScanned(barcode)
                            }
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("BarcodeScanner", "Camera bind failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Elegant overlay with scan region
        Box(modifier = Modifier.fillMaxSize()) {
            // Darkened overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScannerOverlayBg.copy(alpha = 0.3f))
            )

            // Scan target box (center)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.Center)
                    .border(3.dp, ScanBoxBorder, RoundedCornerShape(24.dp))
            )

            // Top controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                IconButton(
                    onClick = {
                        flashEnabled = !flashEnabled
                        camera?.cameraControl?.enableTorch(flashEnabled)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash",
                        tint = Color.White
                    )
                }
            }

            // Bottom instruction text
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = scannerMessage.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = AccentGreen,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = scannerMessage,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Point camera at a barcode",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImage(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onBarcodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { value ->
                        onBarcodeDetected(value)
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
