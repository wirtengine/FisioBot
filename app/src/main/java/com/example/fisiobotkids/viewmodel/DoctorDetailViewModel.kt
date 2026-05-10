package com.example.fisiobotkids.viewmodel

import androidx.lifecycle.*
import com.example.fisiobotkids.data.model.Comandos
import com.example.fisiobotkids.data.model.RobotState
import com.example.fisiobotkids.data.model.SensorData
import com.example.fisiobotkids.data.repository.FisioBotRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DoctorDetailUiState(
    val childId: String = "",
    val childName: String = "",
    val sensorData: SensorData = SensorData(),
    val robotState: RobotState = RobotState(),
    val comandos: Comandos = Comandos(),
    val feedbackMessage: String = ""
)

class DoctorDetailViewModel(
    private val repository: FisioBotRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val childId: String = savedStateHandle["childId"] ?: ""

    private val _uiState = MutableStateFlow(DoctorDetailUiState(childId = childId))
    val uiState: StateFlow<DoctorDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.getSensorData(childId).collect { data ->
                _uiState.update { it.copy(sensorData = data) }
            }
        }
        viewModelScope.launch {
            repository.getRobotState(childId).collect { state ->
                _uiState.update { it.copy(robotState = state) }
            }
        }
    }

    fun enviarComando(modo: String) {
        viewModelScope.launch {
            val nuevos = _uiState.value.comandos.copy(modo = modo)
            repository.enviarComandos(childId, nuevos)
            _uiState.update { it.copy(comandos = nuevos) }
        }
    }

    fun actualizarVelocidad(vel: Float) {
        viewModelScope.launch {
            val nuevos = _uiState.value.comandos.copy(velocidad_max = vel)
            repository.enviarComandos(childId, nuevos)
            _uiState.update { it.copy(comandos = nuevos) }
        }
    }
}

class DoctorDetailViewModelFactory(
    private val repository: FisioBotRepository,
    private val childId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoctorDetailViewModel::class.java)) {
            val handle = SavedStateHandle(mapOf("childId" to childId))
            @Suppress("UNCHECKED_CAST")
            return DoctorDetailViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}