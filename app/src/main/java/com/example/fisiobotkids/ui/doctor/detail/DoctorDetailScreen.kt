package com.example.fisiobotkids.ui.doctor.detail

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fisiobotkids.di.AppContainer
import com.example.fisiobotkids.viewmodel.DoctorDetailViewModel
import com.example.fisiobotkids.viewmodel.DoctorDetailViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailScreen(
    childId: String,
    onBack: () -> Unit
) {
    val viewModel: DoctorDetailViewModel = viewModel(
        factory = DoctorDetailViewModelFactory(AppContainer.repository, childId)
    )
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control - Robot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SensorCard("Distancia", "${state.sensorData.distancia_cm} cm", Icons.Default.Straighten)
            SensorCard("Velocidad", "${state.sensorData.velocidad}", Icons.Default.Speed)
            SensorCard("Encoders", "Izq: ${state.sensorData.encoder_izq}  Der: ${state.sensorData.encoder_der}", Icons.Default.Settings)

            Text(
                text = "Estado: ${state.robotState.modo_actual} | Batería: ${state.robotState.bateria}%",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeButton("Sígueme", Icons.Default.DirectionsRun) { viewModel.enviarComando("sigueme") }
                ModeButton("Baile", Icons.Default.MusicNote) { viewModel.enviarComando("baile") }
                ModeButton("Atrapa", Icons.Default.SportsHandball) { viewModel.enviarComando("atrapa") }
                ModeButton("Stop", Icons.Default.Stop) { viewModel.enviarComando("stop") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SliderWithLabel(
                label = "Velocidad máx: ${state.comandos.velocidad_max}",
                value = state.comandos.velocidad_max,
                onValueChange = { viewModel.actualizarVelocidad(it) },
                valueRange = 0.1f..1.5f
            )

            ChartView()
        }
    }
}

@Composable
fun SensorCard(title: String, value: String, icon: ImageVector) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun ModeButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.padding(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
fun SliderWithLabel(label: String, value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(label)
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

@Composable
fun ChartView() {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                description.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
                val entries = listOf(Entry(1f, 10f), Entry(2f, 20f), Entry(3f, 15f), Entry(4f, 25f))
                val dataSet = LineDataSet(entries, "Progreso").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                }
                data = LineData(dataSet)
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    )
}