package com.example.fisiobotkids.ui.mode

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModeSelectionScreen(
    onDoctorClick: () -> Unit,
    onNinoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FisioBot Kids",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onDoctorClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text("Soy Doctor", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNinoClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text("Soy Niño", style = MaterialTheme.typography.titleMedium)
        }
    }
}