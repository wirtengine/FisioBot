package com.example.fisiobotkids.data.repository

import com.example.fisiobotkids.data.model.*
import kotlinx.coroutines.flow.Flow

interface FisioBotRepository {
    var doctorUid: String?

    fun getSensorData(childId: String): Flow<SensorData>

    fun getRobotState(childId: String): Flow<RobotState>

    suspend fun enviarComandos(childId: String, comandos: Comandos)

    fun getChildrenList(): Flow<List<Child>>

    suspend fun addChild(
        childId: String,
        nombre: String,
        edad: Int
    )

    suspend fun getChildIdByCode(code: String): String?

    // Progreso del niño (niveles)
    suspend fun getChildProgress(childId: String): ChildProgress

    suspend fun saveChildProgress(
        childId: String,
        progress: ChildProgress
    )
}