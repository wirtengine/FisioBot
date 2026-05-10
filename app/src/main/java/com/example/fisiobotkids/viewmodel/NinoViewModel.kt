package com.example.fisiobotkids.viewmodel

import androidx.lifecycle.*
import com.example.fisiobotkids.data.model.SensorData
import com.example.fisiobotkids.data.repository.FisioBotRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NinoUiState(
    val childName: String = "",
    val distancia: Float = 0f,
    val feedbackEmoji: String = "😐",
    val mensaje: String = "Esperando...",
    val modoEjercicio: String = "sigueme"
)

class NinoViewModel(
    private val repository: FisioBotRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val childId: String = savedStateHandle["childId"] ?: ""

    private val _uiState = MutableStateFlow(NinoUiState())
    val uiState: StateFlow<NinoUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.getSensorData(childId).collect { data ->
                val (emoji, mensaje) = evaluateFeedback(data.distancia_cm)
                _uiState.update {
                    it.copy(distancia = data.distancia_cm, feedbackEmoji = emoji, mensaje = mensaje)
                }
            }
        }
    }

    private fun evaluateFeedback(distancia: Float): Pair<String, String> {
        return when {
            distancia in 60f..100f -> "😊" to "¡Bien hecho!"
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