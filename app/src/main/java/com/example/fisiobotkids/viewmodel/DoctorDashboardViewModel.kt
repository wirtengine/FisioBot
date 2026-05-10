package com.example.fisiobotkids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fisiobotkids.data.model.Child
import com.example.fisiobotkids.data.model.SensorData
import com.example.fisiobotkids.data.repository.FisioBotRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val children: List<Child> = emptyList(),
    val liveSensors: Map<String, SensorData> = emptyMap()
)

class DoctorDashboardViewModel(
    private val repository: FisioBotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.getChildrenList().collect { children ->
                _uiState.update { it.copy(children = children) }
                children.forEach { child ->
                    launch {
                        repository.getSensorData(child.id).collect { sensor ->
                            _uiState.update { state ->
                                val map = state.liveSensors.toMutableMap()
                                map[child.id] = sensor
                                state.copy(liveSensors = map)
                            }
                        }
                    }
                }
            }
        }
    }

    fun addChild(childId: String, nombre: String, edad: Int) {
        viewModelScope.launch {
            repository.addChild(childId, nombre, edad)
        }
    }
}

class DoctorDashboardViewModelFactory(
    private val repository: FisioBotRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoctorDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DoctorDashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}