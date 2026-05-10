package com.example.fisiobotkids.di

import com.example.fisiobotkids.data.repository.FirebaseFisioBotRepository
import com.example.fisiobotkids.data.repository.FisioBotRepository
import com.example.fisiobotkids.data.repository.MockFisioBotRepository

object AppContainer {
    private const val USE_MOCK = false // cambiar a false para Firebase real

    val repository: FisioBotRepository by lazy {
        if (USE_MOCK) MockFisioBotRepository() else FirebaseFisioBotRepository()
    }
}