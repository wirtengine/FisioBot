package com.example.fisiobotkids.ui.nino

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.example.fisiobotkids.R
import com.example.fisiobotkids.di.AppContainer
import com.example.fisiobotkids.viewmodel.NinoViewModel
import com.example.fisiobotkids.viewmodel.NinoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NinoScreen(
    childId: String,
    onBack: () -> Unit
) {
    val viewModel: NinoViewModel = viewModel(
        factory = NinoViewModelFactory(AppContainer.repository, childId)
    )
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    // ImageLoader que soporta GIF
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modo Niño") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // GIF animado con Coil
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.ejercicio) // tu GIF
                        .build(),
                    imageLoader = imageLoader
                ),
                contentDescription = "Ejercicio",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Emoji de feedback
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn()
            ) {
                Text(
                    text = state.feedbackEmoji,
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mensaje de feedback
            Text(
                text = state.mensaje,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Distancia actual
            Text(
                text = "Distancia: ${"%.1f".format(state.distancia)} cm",
                style = MaterialTheme.typography.titleMedium
            )

            // Botón de salida
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Salir")
            }
        }
    }
}