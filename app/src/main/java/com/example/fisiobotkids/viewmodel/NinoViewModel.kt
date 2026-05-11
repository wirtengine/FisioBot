package com.example.fisiobotkids.viewmodel

import androidx.lifecycle.*
import com.example.fisiobotkids.data.ExerciseRepository
import com.example.fisiobotkids.data.model.*
import com.example.fisiobotkids.data.repository.FisioBotRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class NinoUiState(
    val screenState: ScreenState = ScreenState.LEVEL_SELECT,
    val allLevels: List<LevelData> = ExerciseRepository.getAllLevels(),
    val unlockedLevel: Int = 1,
    val currentLevelData: LevelData? = null,
    val currentExerciseIndex: Int = 0,
    val currentExercise: ExerciseData? = null,
    val timerSeconds: Int = 60,
    val isTimerRunning: Boolean = false,
    val feedbackEmoji: String = "😐",
    val feedbackMensaje: String = "Esperando...",
    val distancia: Float = 0f,
    val isLevelComplete: Boolean = false
)

enum class ScreenState { LEVEL_SELECT, EXERCISING, LEVEL_COMPLETE }

class NinoViewModel(
    private val repository: FisioBotRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val childId: String = savedStateHandle["childId"] ?: ""

    private val _uiState = MutableStateFlow(NinoUiState())
    val uiState: StateFlow<NinoUiState> = _uiState

    private var timerJob: Job? = null

    init {
        // Cargar progreso desde Firebase (o mock)
        viewModelScope.launch {
            val progress = repository.getChildProgress(childId)
            _uiState.update { it.copy(unlockedLevel = progress.unlockedLevel) }
        }
        // Suscribirse a datos del sensor (distancia)
        viewModelScope.launch {
            repository.getSensorData(childId).collect { sensor ->
                val (emoji, mensaje) = evaluateFeedback(sensor.distancia_cm)
                _uiState.update {
                    it.copy(
                        distancia = sensor.distancia_cm,
                        feedbackEmoji = emoji,
                        feedbackMensaje = mensaje
                    )
                }
            }
        }
    }

    fun startLevel(levelNum: Int) {
        val level = _uiState.value.allLevels.find { it.levelNumber == levelNum } ?: return
        val firstExercise = level.exercises.first()
        _uiState.update {
            it.copy(
                screenState = ScreenState.EXERCISING,
                currentLevelData = level,
                currentExerciseIndex = 0,
                currentExercise = firstExercise,
                timerSeconds = firstExercise.durationSeconds,
                isTimerRunning = true,
                isLevelComplete = false
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value
                if (current.timerSeconds > 1) {
                    _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
                } else {
                    // Tiempo terminado, pasar al siguiente ejercicio
                    nextExercise()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun resumeTimer() {
        if (_uiState.value.screenState == ScreenState.EXERCISING) {
            _uiState.update { it.copy(isTimerRunning = true) }
            startTimer()
        }
    }

    fun nextExercise() {
        val currentState = _uiState.value
        val currentLevel = currentState.currentLevelData ?: return
        val nextIndex = currentState.currentExerciseIndex + 1
        if (nextIndex < currentLevel.exercises.size) {
            val nextEx = currentLevel.exercises[nextIndex]
            _uiState.update {
                it.copy(
                    currentExerciseIndex = nextIndex,
                    currentExercise = nextEx,
                    timerSeconds = nextEx.durationSeconds
                )
            }
            startTimer()
        } else {
            completeLevel()
        }
    }

    private fun completeLevel() {
        timerJob?.cancel()
        val completedLevelNum = _uiState.value.currentLevelData?.levelNumber ?: return
        _uiState.update { it.copy(screenState = ScreenState.LEVEL_COMPLETE, isLevelComplete = true) }

        // Actualizar progreso en Firebase
        viewModelScope.launch {
            try {
                val progress = repository.getChildProgress(childId)
                val newUnlocked = maxOf(progress.unlockedLevel, completedLevelNum + 1)
                val updatedProgress = progress.copy(
                    unlockedLevel = newUnlocked.coerceAtMost(60), // máximo 60
                    completedLevels = progress.completedLevels + (completedLevelNum.toString() to System.currentTimeMillis())
                )
                repository.saveChildProgress(childId, updatedProgress)
                _uiState.update { it.copy(unlockedLevel = newUnlocked) }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun goToLevelSelect() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(screenState = ScreenState.LEVEL_SELECT, isTimerRunning = false)
        }
    }

    private fun evaluateFeedback(distancia: Float): Pair<String, String> {
        return when {
            distancia in 60f..100f -> "😊" to "¡Perfecto!"
            distancia < 60f -> "🔴" to "Aléjate un poco"
            else -> "🟡" to "Acércate un poco más"
        }
    }
}

class NinoViewModelFactory(
    private val repository: FisioBotRepository,
    private val childId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NinoViewModel::class.java)) {
            val handle = SavedStateHandle(mapOf("childId" to childId))
            @Suppress("UNCHECKED_CAST")
            return NinoViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}