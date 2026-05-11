package com.example.fisiobotkids.data

import com.example.fisiobotkids.R
import com.example.fisiobotkids.data.model.ExerciseData
import com.example.fisiobotkids.data.model.LevelData

object ExerciseRepository {
    fun getLevels(): List<LevelData> = listOf(
        // Nivel 1 - Bajo (8 ejercicios)
        LevelData(1, "bajo", listOf(
            ExerciseData("Saludo al sol", "Levanta los brazos", R.drawable.ejercicio, 60),
            ExerciseData("Giro de cadera", "Gira suavemente", R.drawable.ejercicio, 60),
            ExerciseData("Sentadilla", "Agáchate despacio", R.drawable.ejercicio, 60),
            ExerciseData("Estiramiento", "Toca tus pies", R.drawable.ejercicio, 60),
            ExerciseData("Marcha", "Marcha en el lugar", R.drawable.ejercicio, 60),
            ExerciseData("Equilibrio", "Párate en un pie", R.drawable.ejercicio, 60),
            ExerciseData("Brazo arriba", "Levanta un brazo", R.drawable.ejercicio, 60),
            ExerciseData("Descanso activo", "Respira profundo", R.drawable.ejercicio, 60)
        )),
        // Nivel 2 - Bajo
        // ... (repetiremos con ligeras variaciones, por simplicidad usaremos los mismos GIFs de momento)
        LevelData(2, "bajo", listOf(
            ExerciseData("Círculos de brazos", "Gira los brazos", R.drawable.ejercicio, 60),
            // ... 7 más
        )),
        // Añade más niveles hasta 20 con dificultad creciente
    ).apply {
        // Como es ejemplo, rellenamos niveles 3-20 con ejercicios de ejemplo
        for (i in 3..20) {
            val diff = when {
                i <= 7 -> "bajo"
                i <= 14 -> "intermedio"
                else -> "avanzado"
            }
            // crea una lista rápida de ejercicios genéricos
            this.plus(LevelData(i, diff, List(8) { index ->
                ExerciseData("Ejercicio $index", "Sigue las instrucciones", R.drawable.ejercicio, 60)
            }))
        }
    }
}