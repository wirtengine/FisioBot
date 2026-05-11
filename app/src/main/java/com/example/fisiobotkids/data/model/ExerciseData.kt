package com.example.fisiobotkids.data.model

data class ExerciseData(
    val name: String,
    val instruction: String,
    val gifResId: Int,           // referencia al recurso drawable
    val durationSeconds: Int = 60
)