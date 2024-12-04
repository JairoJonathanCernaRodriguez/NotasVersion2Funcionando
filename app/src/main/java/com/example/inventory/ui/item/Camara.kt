package com.example.inventory.ui.item

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun CameraButton() {
    var imageBitmaps by remember { mutableStateOf<List<Bitmap>>(listOf()) }
    var videoUris by remember { mutableStateOf<List<Uri>>(listOf()) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Launcher para tomar foto
    val openCameraForPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imageBitmaps = imageBitmaps + bitmap
        }
    }

    // Crear la URI para el video
    val videoUri = remember { mutableStateOf<Uri?>(null) }

    // Launcher para grabar video
    val openCameraForVideo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { isSuccessful ->
        if (isSuccessful) {
            videoUri.value?.let {
                videoUris = videoUris + it
            }
        } else {
            Toast.makeText(context, "Error al grabar el video", Toast.LENGTH_SHORT).show()
        }
    }

    // Solicitar permisos
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Column {
        Row {
            // Botón para tomar una foto
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    hasCameraPermission = true
                    openCameraForPhoto.launch(null)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Icon(Icons.Filled.AccountBox, contentDescription = "Abrir cámara para fotos")
                Text("Foto")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón para grabar video
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Crear URI para el video
                    val uri = createInternalVideoUri(context)
                    if (uri != null) {
                        videoUri.value = uri
                        openCameraForVideo.launch(uri)
                    } else {
                        Toast.makeText(context, "Error al crear el archivo para el video", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Icon(Icons.Filled.Done, contentDescription = "Abrir cámara para video")
                Text("Video")
            }
        }

        // Mostrar las imágenes capturadas
        LazyColumn(modifier = Modifier.height(150.dp)) {
            items(imageBitmaps) { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Imagen capturada",
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .clickable {
                            selectedBitmap = bitmap
                        }
                )
            }
        }

        // Mostrar los videos grabados
        LazyColumn(modifier = Modifier.height(150.dp)) {
            items(videoUris) { uri ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Video: $uri")
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setDataAndType(uri, "video/*")
                        context.startActivity(intent)
                    }) {
                        Text("Reproducir")
                    }
                }
            }
        }
    }
}

// Función para obtener el tipo MIME de un archivo dado su URI
fun getMimeType(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)
}

// Función para crear una URI interna para el video
fun createInternalVideoUri(context: Context): Uri? {
    val videoDir = File(context.filesDir, "videos")
    if (!videoDir.exists()) {
        videoDir.mkdirs()
    }
    val videoFile = File(videoDir, "video_${System.currentTimeMillis()}.mp4")
    return try {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null // Devuelve nulo si hay un error
    }
}
