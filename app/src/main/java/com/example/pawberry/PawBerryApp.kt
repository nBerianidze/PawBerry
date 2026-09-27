package com.example.pawberry

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pawberry.data.PawBerryViewModel
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.navigation.BottomNavItem
import com.example.pawberry.navigation.Routes
import com.example.pawberry.ui.AddReminderScreen
import com.example.pawberry.ui.CategoryHistoryScreen
import com.example.pawberry.ui.HomeScreen
import com.example.pawberry.ui.LoginScreen
import com.example.pawberry.ui.MyPetsScreen
import com.example.pawberry.ui.PetFormScreen
import com.example.pawberry.ui.PetProfileScreen
import com.example.pawberry.ui.RegisterScreen
import com.example.pawberry.ui.RemindersScreen

@Composable
fun PawBerryApp(
    viewModel: PawBerryViewModel = viewModel(factory = PawBerryViewModel.Factory),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = BottomNavItem.entries.any { it.route == currentRoute }

    // One observed copy of the database state, shared by every screen below.
    val username by viewModel.username.collectAsStateWithLifecycle()
    val pets by viewModel.pets.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val upcomingReminders by viewModel.upcomingReminders.collectAsStateWithLifecycle()
    val rewardPoints by viewModel.rewardPoints.collectAsStateWithLifecycle()
    val formError by viewModel.formError.collectAsStateWithLifecycle()

    // The error belongs to whichever form is on screen, so leaving a screen clears it.
    LaunchedEffect(currentRoute) { viewModel.clearFormError() }

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
                                    // Keep a single copy of each tab and remember where
                                    // the user was when they come back to it.
                                    popUpTo(Routes.HOME) { saveState = true }
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
                    errorMessage = formError,
                    onLogin = { enteredUsername, password ->
                        viewModel.login(enteredUsername, password) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    },
                    onSignUp = { navController.navigate(Routes.REGISTER) },
                    onClearError = viewModel::clearFormError,
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    errorMessage = formError,
                    onRegister = { name, surname, newUsername, email, phone, dob, password, confirm ->
                        viewModel.register(
                            name = name,
                            surname = surname,
                            username = newUsername,
                            email = email,
                            phoneNumber = phone,
                            dateOfBirth = dob,
                            password = password,
                            confirmPassword = confirm,
                        ) {
                            // Account created: send them back to log in with it.
                            navController.popBackStack()
                        }
                    },
                    onBackToLogin = { navController.popBackStack() },
                    onClearError = viewModel::clearFormError,
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    username = username,
                    petCount = pets.size,
                    upcomingReminders = upcomingReminders,
                    rewardPoints = rewardPoints,
                    onViewPets = { navController.navigate(Routes.MY_PETS) },
                    onViewReminders = { navController.navigate(Routes.REMINDERS) },
                )
            }

            composable(Routes.MY_PETS) {
                MyPetsScreen(
                    pets = pets,
                    onPetClick = { petId -> navController.navigate(Routes.petProfile(petId)) },
                    onAddPet = { navController.navigate(Routes.ADD_PET) },
                )
            }

            composable(Routes.ADD_PET) {
                PetFormScreen(
                    title = "Add a pet",
                    submitLabel = "Add Pet",
                    errorMessage = formError,
                    onSubmit = { name, breed, dob, toy ->
                        viewModel.addPet(name, breed, dob, toy) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onClearError = viewModel::clearFormError,
                )
            }

            composable(
                route = Routes.PET_PROFILE,
                arguments = listOf(navArgument(Routes.PET_ID_ARG) { type = NavType.LongType }),
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getLong(Routes.PET_ID_ARG) ?: return@composable
                val pet = pets.firstOrNull { it.petId == petId }
                val countByCategory = reminders
                    .filter { it.reminder.petId == petId }
                    .groupingBy { ReminderCategory.fromStored(it.reminder.category) }
                    .eachCount()

                PetProfileScreen(
                    pet = pet,
                    reminderCountByCategory = countByCategory,
                    onCategoryClick = { category ->
                        navController.navigate(Routes.petCategory(petId, category.name))
                    },
                    onEditPet = { navController.navigate(Routes.editPet(petId)) },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.EDIT_PET,
                arguments = listOf(navArgument(Routes.PET_ID_ARG) { type = NavType.LongType }),
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getLong(Routes.PET_ID_ARG) ?: return@composable
                val pet = pets.firstOrNull { it.petId == petId } ?: return@composable

                PetFormScreen(
                    title = "Edit ${pet.name}",
                    submitLabel = "Save Changes",
                    errorMessage = formError,
                    onSubmit = { name, breed, dob, toy ->
                        viewModel.updatePet(pet, name, breed, dob, toy) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onClearError = viewModel::clearFormError,
                    initialName = pet.name,
                    initialBreed = pet.breed,
                    initialDateOfBirth = pet.dateOfBirth,
                    initialFavoriteToy = pet.favoriteToy,
                )
            }

            composable(
                route = Routes.PET_CATEGORY,
                arguments = listOf(
                    navArgument(Routes.PET_ID_ARG) { type = NavType.LongType },
                    navArgument(Routes.CATEGORY_ARG) { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getLong(Routes.PET_ID_ARG) ?: return@composable
                val category = ReminderCategory.fromStored(
                    backStackEntry.arguments?.getString(Routes.CATEGORY_ARG).orEmpty()
                )
                val pet = pets.firstOrNull { it.petId == petId }
                val categoryReminders = reminders
                    .map { it.reminder }
                    .filter { it.petId == petId && it.category == category.name }

                CategoryHistoryScreen(
                    petName = pet?.name.orEmpty(),
                    category = category,
                    reminders = categoryReminders,
                    onCompletedChange = viewModel::setReminderCompleted,
                    onAddReminder = {
                        navController.navigate(Routes.addReminder(petId, category.name))
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.REMINDERS) {
                RemindersScreen(
                    reminders = reminders,
                    onCompletedChange = viewModel::setReminderCompleted,
                    onAddReminder = { navController.navigate(Routes.addReminder()) },
                )
            }

            composable(
                route = Routes.ADD_REMINDER,
                arguments = listOf(
                    navArgument(Routes.PET_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = NO_PET_ID
                    },
                    navArgument(Routes.CATEGORY_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { backStackEntry ->
                val arguments = backStackEntry.arguments
                val preselectedPetId = arguments?.getLong(Routes.PET_ID_ARG)
                    ?.takeIf { it != NO_PET_ID }
                val preselectedCategory = arguments?.getString(Routes.CATEGORY_ARG)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { ReminderCategory.fromStored(it) }

                AddReminderScreen(
                    pets = pets,
                    errorMessage = formError,
                    onSave = { petId, title, category, dueDate, dueTime, repeat ->
                        viewModel.addReminder(petId, title, category, dueDate, dueTime, repeat) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onClearError = viewModel::clearFormError,
                    preselectedPetId = preselectedPetId,
                    preselectedCategory = preselectedCategory,
                )
            }
        }
    }
}

/** Sentinel for "no pet pre-selected" on the optional Add Reminder argument. */
private const val NO_PET_ID = -1L
