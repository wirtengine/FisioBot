package com.example.fisiobotkids.data.model

data class ChildProgress(
    val unlockedLevel: Int = 1,
    val completedLevels: Map<String, Long> = emptyMap()
)