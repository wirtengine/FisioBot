package com.example.fisiobotkids.data.model

data class LevelData(
    val levelNumber: Int,
    val difficulty: String,     // "básico", "intermedio", "avanzado"
    val exercises: List<ExerciseData>
)