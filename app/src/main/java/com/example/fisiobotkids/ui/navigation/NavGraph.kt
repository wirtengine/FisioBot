package com.example.fisiobotkids.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fisiobotkids.ui.doctor.dashboard.DoctorDashboardScreen
import com.example.fisiobotkids.ui.doctor.detail.DoctorDetailScreen
import com.example.fisiobotkids.ui.login.LoginScreen
import com.example.fisiobotkids.ui.mode.ModeSelectionScreen
import com.example.fisiobotkids.ui.nino.NinoLoginScreen
import com.example.fisiobotkids.ui.nino.NinoScreen
import com.example.fisiobotkids.viewmodel.AuthViewModel

@Composable
fun FisioNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    NavHost(
        navController = navController,
        startDestination = "mode_selection"
    ) {
        composable("mode_selection") {
            ModeSelectionScreen(
                onDoctorClick = {
                    if (authViewModel.uiState.value.userEmail != null) {
                        navController.navigate("doctor_dashboard") {
                            popUpTo("mode_selection") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login")
                    }
                },
                onNinoClick = {
                    navController.navigate("nino_login")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("doctor_dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable("doctor_dashboard") {
            DoctorDashboardScreen(
                onChildClick = { childId ->
                    navController.navigate("doctor_detail/$childId")
                },
                onNinoMode = { childId ->
                    navController.navigate("nino_mode/$childId")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("mode_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            "doctor_detail/{childId}",
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
            DoctorDetailScreen(
                childId = childId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("nino_login") {
            NinoLoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { childId ->
                    navController.navigate("nino_mode/$childId") {
                        popUpTo("nino_login") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "nino_mode/{childId}",
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
            NinoScreen(
                childId = childId,
                onBack = {
                    authViewModel.logout()  // cerrar sesión del niño
                    navController.navigate("mode_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}