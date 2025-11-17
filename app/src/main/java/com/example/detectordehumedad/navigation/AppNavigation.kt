package com.example.detectordehumedad.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.detectordehumedad.auth.AuthViewModel
import com.example.detectordehumedad.ui.home.HomeScreen
import com.example.detectordehumedad.ui.login.LoginScreen
import com.example.detectordehumedad.ui.theme.ThemeViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val themeViewModel: ThemeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("home") {
            HomeScreen(navController, authViewModel, themeViewModel = themeViewModel)
        }
    }
}
