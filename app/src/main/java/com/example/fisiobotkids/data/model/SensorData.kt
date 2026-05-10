package com.example.fisiobotkids.data.model

data class SensorData(
    val distancia_cm: Float = 0f,
    val velocidad: Float = 0f,
    val encoder_izq: Int = 0,
    val encoder_der: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)