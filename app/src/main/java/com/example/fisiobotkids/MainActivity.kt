package com.example.fisiobotkids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.fisiobotkids.ui.navigation.FisioNavGraph
import com.example.fisiobotkids.ui.theme.FisioBotKidsTheme
import com.example.fisiobotkids.viewmodel.AuthViewModel
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        enableEdgeToEdge()
        setContent {
            FisioBotKidsTheme {
                val navController = rememberNavController()
                val authViewModel = viewModel<AuthViewModel>()
                FisioNavGraph(navController, authViewModel)
            }
        }
    }
}