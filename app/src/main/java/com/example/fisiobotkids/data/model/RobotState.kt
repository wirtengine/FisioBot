package com.example.fisiobotkids.data.model

data class RobotState(
    val modo_actual: String = "stop",
    val bateria: Int = 100,
    val conectado: Boolean = false
)