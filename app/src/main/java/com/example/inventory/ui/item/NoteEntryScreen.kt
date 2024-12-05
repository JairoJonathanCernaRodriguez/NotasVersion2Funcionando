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

    val context = LocalContext.current
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
                    // Botones organizados en filas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { pickImage.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Select Image")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { pickImage.launch("video/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Select Video")
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                val imageFile = File.createTempFile(
                                    "temp_photo_${System.currentTimeMillis()}",
                                    ".jpg",
                                    context.cacheDir
                                )
                                val tempUri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    imageFile
                                )
                                capturedMediaUri = tempUri
                                takePictureLauncher.launch(tempUri)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Open Camera")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                val videoFile = File.createTempFile(
                                    "temp_video_${System.currentTimeMillis()}",
                                    ".mp4",
                                    context.cacheDir
                                )
                                val tempUri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    videoFile
                                )
                                capturedMediaUri = tempUri
                                captureVideoLauncher.launch(tempUri)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Capture Video")
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Button(
                            onClick = { showAudioRecorderDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Record Audio")
                        }
                    }

                    // Multimedia (si existen elementos en multimediaUris)
                    if (multimediaUris.isNotEmpty()) {
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

    if (showDatePicker) {
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

    if (showTimePicker) {
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

    if (showAudioRecorderDialog) {
        AlertDialog(
            onDismissRequest = { showAudioRecorderDialog = false },
            title = { Text("Audio Recorder") },
            text = { AudioRecorderButton() },
            confirmButton = {
                Button(onClick = { showAudioRecorderDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}



