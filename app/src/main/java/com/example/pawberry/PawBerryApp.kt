package com.example.pawberry

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pawberry.data.PawBerryViewModel
import com.example.pawberry.navigation.BottomNavItem
import com.example.pawberry.navigation.Routes
import com.example.pawberry.ui.AddPetScreen
import com.example.pawberry.ui.HomeScreen
import com.example.pawberry.ui.LoginScreen
import com.example.pawberry.ui.MyPetsScreen
import com.example.pawberry.ui.RegisterScreen
import com.example.pawberry.ui.RemindersScreen

@Composable
fun PawBerryApp(
    viewModel: PawBerryViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = BottomNavItem.entries.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLogin = { username, password ->
                        val error = viewModel.login(username, password)
                        if (error == null) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                        error
                    },
                    onSignUp = { navController.navigate(Routes.REGISTER) },
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegister = { name, surname, email, phone, dob ->
                        viewModel.register(name, surname, email, phone, dob)
                    },
                    onBackToLogin = { navController.popBackStack() },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(username = viewModel.currentUsername)
            }
            composable(Routes.MY_PETS) {
                MyPetsScreen(
                    pets = viewModel.pets.filter {
                        it.ownerUsername.equals(viewModel.currentUsername, ignoreCase = true)
                    },
                    onAddPet = { navController.navigate(Routes.ADD_PET) },
                )
            }
            composable(Routes.ADD_PET) {
                AddPetScreen(
                    onSubmit = { name, breed, dob, toy ->
                        viewModel.addPet(name, breed, dob, toy)
                    },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(Routes.REMINDERS) {
                RemindersScreen()
            }
        }
    }
}
