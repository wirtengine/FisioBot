package com.example.fisiobotkids.ui.nino

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.fisiobotkids.data.model.ExerciseData
import com.example.fisiobotkids.data.model.LevelData
import com.example.fisiobotkids.di.AppContainer
import com.example.fisiobotkids.viewmodel.NinoViewModel
import com.example.fisiobotkids.viewmodel.NinoViewModelFactory
import com.example.fisiobotkids.viewmodel.ScreenState

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.screenState) {
                            ScreenState.LEVEL_SELECT -> "Niveles"
                            ScreenState.EXERCISING -> state.currentLevelData?.let { "Nivel ${it.levelNumber}" } ?: "Ejercicio"
                            ScreenState.LEVEL_COMPLETE -> "¡Nivel Completado!"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.screenState == ScreenState.LEVEL_SELECT) onBack()
                        else viewModel.goToLevelSelect()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state.screenState) {
                ScreenState.LEVEL_SELECT -> LevelSelectScreen(
                    allLevels = state.allLevels,
                    unlockedLevel = state.unlockedLevel,
                    onLevelClick = { viewModel.startLevel(it) }
                )
                ScreenState.EXERCISING -> ExerciseScreen(
                    exercise = state.currentExercise,
                    timerSeconds = state.timerSeconds,
                    feedbackEmoji = state.feedbackEmoji,
                    feedbackMensaje = state.feedbackMensaje,
                    distancia = state.distancia,
                    onPause = { viewModel.pauseTimer() },
                    onResume = { viewModel.resumeTimer() }
                )
                ScreenState.LEVEL_COMPLETE -> LevelCompleteScreen(
                    onContinue = { viewModel.goToLevelSelect() }
                )
            }
        }
    }
}

@Composable
fun LevelSelectScreen(
    allLevels: List<LevelData>,
    unlockedLevel: Int,
    onLevelClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(allLevels) { level ->
            val isUnlocked = level.levelNumber <= unlockedLevel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isUnlocked) { onLevelClick(level.levelNumber) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isUnlocked) {
                        Icon(Icons.Default.Lock, contentDescription = "Bloqueado", tint = Color.Gray)
                        Text("Nivel ${level.levelNumber}", color = Color.Gray)
                    } else {
                        Text("Nivel ${level.levelNumber}", style = MaterialTheme.typography.titleMedium)
                        Text(level.difficulty, style = MaterialTheme.typography.bodySmall)
                        Text("${level.exercises.size} ejercicios", style = MaterialTheme.typography.bodySmall)
                        Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar")
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseScreen(
    exercise: ExerciseData?,
    timerSeconds: Int,
    feedbackEmoji: String,
    feedbackMensaje: String,
    distancia: Float,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    if (exercise == null) return
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context).components { add(GifDecoder.Factory()) }.build()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // GIF del ejercicio
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context).data(exercise.gifResId).build(),
                imageLoader = imageLoader
            ),
            contentDescription = exercise.name,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nombre e instrucción
        Text(text = exercise.name, style = MaterialTheme.typography.headlineSmall)
        Text(text = exercise.instruction, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Temporizador
        Text(
            text = formatTime(timerSeconds),
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Botones de control
        Row {
            Button(onClick = onPause) { Text("Pausa") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onResume) { Text("Reanudar") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feedback en tiempo real
        AnimatedVisibility(visible = true) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(feedbackEmoji, fontSize = 48.sp)
                Text(feedbackMensaje, style = MaterialTheme.typography.bodyLarge)
                Text("Distancia: ${"%.1f".format(distancia)} cm", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun LevelCompleteScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Felicidades!", style = MaterialTheme.typography.headlineLarge)
        Text("Has completado el nivel.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onContinue) { Text("Volver") }
    }
}

private fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%02d:%02d".format(min, sec)
}