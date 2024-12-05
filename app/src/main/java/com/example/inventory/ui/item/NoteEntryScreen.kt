package com.example.inventory.ui.item

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.notes.NoteDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEntryScreen(
    navigateBack: () -> Unit,
    viewModel: NoteEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val noteUiState by viewModel.noteUiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showMultimediaPicker by remember { mutableStateOf(false) }
    var multimediaUris by remember { mutableStateOf<List<String>>(listOf()) }
    var showCameraDialog by remember { mutableStateOf(false) }
    var showAudioRecorderDialog by remember { mutableStateOf(false) }

    // Registrar actividad para elegir imagen
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            multimediaUris = multimediaUris + it.toString()
            viewModel.updateMultimediaUris(multimediaUris)
        }
    }

    // Registrar actividad para elegir archivo
    val pickDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            multimediaUris = multimediaUris + it.toString()
            viewModel.updateMultimediaUris(multimediaUris)
        }
    }

    // Estado para capturas de cámara y video
    var capturedMediaUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para tomar fotos con la cámara
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && capturedMediaUri != null) {
            multimediaUris = multimediaUris + capturedMediaUri.toString()
            viewModel.updateMultimediaUris(multimediaUris)
        }
    }

    // Launcher para capturar video con la cámara
    val captureVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && capturedMediaUri != null) {
            multimediaUris = multimediaUris + capturedMediaUri.toString()
            viewModel.updateMultimediaUris(multimediaUris)
        }
    }

    // Contexto local para acceso a los recursos
    val context = LocalContext.current

    // Estado para seleccionar entre Notas o Recordatorios
    var isReminderView by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = if (isReminderView) "Add Reminder" else "Add Note",
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {

                // Switch para cambiar entre Recordatorios y Notas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isReminderView) "Recordatorios" else "Notas",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Switch(
                        checked = isReminderView,
                        onCheckedChange = { isReminderView = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                // Título del input
                OutlinedTextField(
                    value = noteUiState.noteDetails?.title.orEmpty(),
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Title") },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )

                // Contenido del input
                OutlinedTextField(
                    value = noteUiState.noteDetails?.content.orEmpty(),
                    onValueChange = { viewModel.updateContent(it) },
                    label = { Text("Content") },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )

                // Mostrar campos adicionales si está en "Recordatorios"
                if (isReminderView) {
                    // Seleccionar Fecha
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Select Date")
                    }
                    Text(
                        text = "Selected Date: ${
                            noteUiState.noteDetails?.fecha?.takeIf { it != 0L }?.let {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                            } ?: "Not selected"
                        }",
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    // Seleccionar Hora
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Select Time")
                    }
                    Text(
                        text = "Selected Time: ${
                            noteUiState.noteDetails?.hora?.takeIf { it != 0L }?.let {
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
                            } ?: "Not selected"
                        }",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else {
                    // Botón de selección de imagen
                    Button(
                        onClick = { pickImage.launch("image/*") },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Select Image")
                    }
                    // Botón de selección de video
                    Button(
                        onClick = { pickImage.launch("video/*") },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Select Video")
                    }

                    // Botón de selección de documento
                    Button(
                        onClick = { pickDocument.launch(arrayOf("/")) },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Select Document")
                    }

                    // Botón para tomar foto
                    Button(
                        onClick = {
                            // Genera un archivo temporal
                            val imageFile = File.createTempFile(
                                "temp_photo_${System.currentTimeMillis()}",
                                ".jpg",
                                context.cacheDir
                            )

                            // Obtiene el URI del archivo
                            val tempUri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                imageFile
                            )

                            // Asigna el URI a la propiedad
                            capturedMediaUri = tempUri

                            // Lanza el intent con el URI almacenado
                            takePictureLauncher.launch(tempUri)
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Open Camera")
                    }
                    // Botón para grabar video
                    Button(
                        onClick = {
                            // Genera un archivo temporal
                            val videoFile = File.createTempFile(
                                "temp_video_${System.currentTimeMillis()}",
                                ".mp4",
                                context.cacheDir
                            )

                            // Obtiene el URI del archivo
                            val tempUri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                videoFile
                            )

                            // Asigna el URI a la propiedad
                            capturedMediaUri = tempUri

                            // Lanza el intent con el URI almacenado
                            captureVideoLauncher.launch(tempUri)
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Capture Video")
                    }




                    // Botón para mostrar el diálogo de grabadora de audio
                    Button(
                        onClick = { showAudioRecorderDialog = true },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Record Audio")
                    }

                    // Multimedia (solo en "Notas")
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(150.dp)
                    ) {
                        items(multimediaUris) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(model = Uri.parse(uri)),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .width(100.dp)
                                    .height(150.dp)
                            )
                        }
                    }
                }

                // Botón para guardar la nota
                Button(
                    onClick = {
                        viewModel.saveNote()
                        navigateBack()
                    },
                    enabled = noteUiState.isEntryValid,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Save Note")
                }
            }
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        val context = LocalContext.current
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                viewModel.updateFecha(calendar.timeInMillis)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val context = LocalContext.current
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                viewModel.updateHora(calendar.timeInMillis)
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
    if (showCameraDialog) {
        AlertDialog(
            onDismissRequest = { showCameraDialog = false },
            title = { Text("Camera") },
            text = {
                CameraButton()
            },
            confirmButton = {
                Button(onClick = { showCameraDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAudioRecorderDialog) {
        AlertDialog(
            onDismissRequest = { showAudioRecorderDialog = false },
            title = { Text("Audio Recorder") },
            text = {
                AudioRecorderButton()
            },
            confirmButton = {
                Button(onClick = { showAudioRecorderDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}


