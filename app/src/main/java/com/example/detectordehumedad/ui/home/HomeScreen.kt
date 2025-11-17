package com.example.detectordehumedad.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.detectordehumedad.auth.AuthViewModel
import com.example.detectordehumedad.data.DataViewModel
import com.example.detectordehumedad.data.HumidityRecord
import com.example.detectordehumedad.data.HumidityState
import com.example.detectordehumedad.data.RecordsState
import com.example.detectordehumedad.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    dataViewModel: DataViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<HumidityRecord?>(null) }
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    if (showDialog && selectedRecord != null) {
        UpdateDialog(record = selectedRecord!!, viewModel = dataViewModel) {
            showDialog = false
            selectedRecord = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detector de Humedad") },
                actions = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { themeViewModel.toggleTheme() }
                    )
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(text = { Text("Cerrar sesión") }, onClick = {
                            authViewModel.signOut()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        })
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CurrentHumiditySection(dataViewModel)
            Spacer(modifier = Modifier.height(24.dp))
            HumidityRecordsSection(dataViewModel) {
                selectedRecord = it
                showDialog = true
            }
        }
    }
}

@Composable
fun CurrentHumiditySection(dataViewModel: DataViewModel) {
    val humidityState by dataViewModel.humidityState.collectAsState()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = humidityState) {
                is HumidityState.Success -> {
                    Text("Humedad Actual", style = MaterialTheme.typography.titleMedium)
                    Text("${state.humidity}%", style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(vertical = 8.dp))
                    Button(onClick = { dataViewModel.addHumidityRecord(state.humidity) }) {
                        Text("Guardar Medición")
                    }
                }
                is HumidityState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    CircularProgressIndicator()
                    Text("Cargando...", modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun HumidityRecordsSection(dataViewModel: DataViewModel, onEdit: (HumidityRecord) -> Unit) {
    val recordsState by dataViewModel.recordsState.collectAsState()

    Text("Historial de Mediciones", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

    when (val state = recordsState) {
        is RecordsState.Success -> {
            if (state.records.isEmpty()) {
                Text("No hay mediciones guardadas.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.records) { record ->
                        HumidityRecordItem(record, dataViewModel, onEdit)
                    }
                }
            }
        }
        is RecordsState.Error -> {
            Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        is RecordsState.Loading -> {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun HumidityRecordItem(
    record: HumidityRecord,
    viewModel: DataViewModel,
    onEdit: (HumidityRecord) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Humedad: ${record.humidity}%", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Row {
                IconButton(onClick = { onEdit(record) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { viewModel.deleteHumidityRecord(record.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
fun UpdateDialog(
    record: HumidityRecord,
    viewModel: DataViewModel,
    onDismiss: () -> Unit
) {
    var updatedHumidity by remember { mutableStateOf(record.humidity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actualizar Humedad") },
        text = {
            OutlinedTextField(
                value = updatedHumidity,
                onValueChange = { updatedHumidity = it },
                label = { Text("Nuevo valor de humedad") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                val newHumidity = updatedHumidity.toIntOrNull()
                if (newHumidity != null) {
                    viewModel.updateHumidityRecord(record.copy(humidity = newHumidity))
                    onDismiss()
                }
            }) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}