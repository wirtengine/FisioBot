package com.example.fisiobotkids.data.model

data class Comandos(
    val modo: String = "stop",
    val velocidad_max: Float = 0.7f,
    val distancia_min: Int = 50,
    val distancia_max: Int = 150,
    val detener: Boolean = false
)