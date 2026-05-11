package com.example.fisiobotkids.data.repository

import com.example.fisiobotkids.data.model.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

class MockFisioBotRepository : FisioBotRepository {

    override var doctorUid: String? = null

    private val childrenList = MutableStateFlow(
        listOf(
            Child(id = "child1", nombre = "María", edad = 7, codigo = "1234"),
            Child(id = "child2", nombre = "Carlos", edad = 9, codigo = "5678")
        )
    )

    private val sensorFlows = mutableMapOf<String, MutableStateFlow<SensorData>>()
    private val stateFlows = mutableMapOf<String, MutableStateFlow<RobotState>>()

    // Progreso de los niños
    private val progressMap = mutableMapOf<String, ChildProgress>()

    private fun getOrCreateSensorFlow(childId: String) =
        sensorFlows.getOrPut(childId) {
            MutableStateFlow(
                SensorData(
                    distancia_cm = 100f,
                    velocidad = 0.5f
                )
            )
        }

    private fun getOrCreateStateFlow(childId: String) =
        stateFlows.getOrPut(childId) {
            MutableStateFlow(RobotState())
        }

    override fun getSensorData(childId: String): Flow<SensorData> =
        getOrCreateSensorFlow(childId)

    override fun getRobotState(childId: String): Flow<RobotState> =
        getOrCreateStateFlow(childId)

    override suspend fun enviarComandos(
        childId: String,
        comandos: Comandos
    ) {
        val state = getOrCreateStateFlow(childId)
        val sensor = getOrCreateSensorFlow(childId)

        state.value = state.value.copy(
            modo_actual = comandos.modo
        )

        when (comandos.modo) {
            "sigueme" -> {
                sensor.value = sensor.value.copy(
                    distancia_cm = Random.nextFloat() * 150f,
                    velocidad = comandos.velocidad_max
                )
            }

            "stop" -> {
                sensor.value = sensor.value.copy(
                    velocidad = 0f
                )
            }
        }
    }

    override fun getChildrenList(): Flow<List<Child>> = childrenList

    override suspend fun addChild(
        childId: String,
        nombre: String,
        edad: Int
    ) {
        val codigo = (1000..9999).random().toString()

        val newChild = Child(
            id = childId,
            nombre = nombre,
            edad = edad,
            codigo = codigo
        )

        val updated = childrenList.value + newChild
        childrenList.value = updated
    }

    override suspend fun getChildIdByCode(code: String): String? {
        return childrenList.value.find {
            it.codigo == code
        }?.id
    }

    override suspend fun getChildProgress(childId: String): ChildProgress =
        progressMap[childId] ?: ChildProgress()

    override suspend fun saveChildProgress(
        childId: String,
        progress: ChildProgress
    ) {
        progressMap[childId] = progress
    }
}