package com.example.fisiobotkids.ui.doctor.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fisiobotkids.di.AppContainer
import com.example.fisiobotkids.data.model.Child
import com.example.fisiobotkids.data.model.SensorData
import com.example.fisiobotkids.viewmodel.AuthViewModel
import com.example.fisiobotkids.viewmodel.DoctorDashboardViewModel
import com.example.fisiobotkids.viewmodel.DoctorDashboardViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    onChildClick: (String) -> Unit,
    onNinoMode: (String) -> Unit,
    onLogout: () -> Unit
) {
    val authViewModel = viewModel<AuthViewModel>()
    val viewModel: DoctorDashboardViewModel = viewModel(
        factory = DoctorDashboardViewModelFactory(AppContainer.repository)
    )
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Asignar el doctorUid actual al repositorio
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        AppContainer.repository.doctorUid = uid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Pacientes") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir paciente")
            }
        }
    ) { padding ->
        if (state.children.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay pacientes registrados.\nAñade uno con el botón +")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(state.children) { child ->
                    ChildCard(
                        child = child,
                        sensorData = state.liveSensors[child.id],
                        onDoctorClick = { onChildClick(child.id) },
                        onNinoClick = { onNinoMode(child.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddChildDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre, edad, email, password ->
                authViewModel.registerChild(
                    email = email,
                    password = password,
                    onSuccess = { childUid ->
                        viewModel.addChild(childUid, nombre, edad)
                        showAddDialog = false
                    },
                    onError = { error ->
                        // El error se muestra dentro del diálogo
                    }
                )
            }
        )
    }
}

@Composable
fun ChildCard(
    child: Child,
    sensorData: SensorData?,
    onDoctorClick: () -> Unit,
    onNinoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onDoctorClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = child.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(text = "${child.edad} años", style = MaterialTheme.typography.bodySmall)
                }
                val color = when {
                    sensorData == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    sensorData.distancia_cm in 60f..100f -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                }
                Icon(
                    Icons.Default.Circle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Distancia: ${sensorData?.distancia_cm?.let { "%.1f cm".format(it) } ?: "--"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Código: ${child.codigo.ifBlank { "Sin código" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDoctorClick) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Control")
                }
                TextButton(onClick = onNinoClick) {
                    Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Modo Niño")
                }
            }
        }
    }
}

@Composable
fun AddChildDialog(
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, edad: Int, email: String, password: String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Paciente") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = edad,
                    onValueChange = { edad = it.filter { c -> c.isDigit() } },
                    label = { Text("Edad") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text("Email del niño") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val edadInt = edad.toIntOrNull() ?: 0
                when {
                    nombre.isBlank() -> errorMessage = "El nombre es obligatorio"
                    edadInt <= 0 -> errorMessage = "Edad no válida"
                    !email.contains("@") -> errorMessage = "Email incorrecto"
                    password.length < 6 -> errorMessage = "La contraseña debe tener al menos 6 caracteres"
                    else -> onConfirm(nombre.trim(), edadInt, email, password)
                }
            }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}