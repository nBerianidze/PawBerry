package com.example.pawberry

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

    // One observed copy of backend-backed session state, shared by every screen below.
    val username by viewModel.username.collectAsStateWithLifecycle()
    val pets by viewModel.pets.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val upcomingReminders by viewModel.upcomingReminders.collectAsStateWithLifecycle()
    val rewardPoints by viewModel.rewardPoints.collectAsStateWithLifecycle()
    val formError by viewModel.formError.collectAsStateWithLifecycle()
    val emailError by viewModel.emailError.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val backendStatus by viewModel.backendStatus.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val imageBaseUrl = viewModel.imageBaseUrl()

    LaunchedEffect(userMessage) {
        val message = userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearUserMessage()
    }

    // The error belongs to whichever form is on screen, so leaving a screen clears it.
    LaunchedEffect(currentRoute) {
        viewModel.clearFormError()
        if (currentRoute == Routes.LOGIN) {
            viewModel.checkBackendConnection()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                if (item.tintIcon) {
                                    Icon(
                                        painter = painterResource(item.icon),
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(item.icon),
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    errorMessage = formError,
                    backendStatus = backendStatus,
                    isBusy = isBusy,
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
                    emailError = emailError,
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
                    isBusy = isBusy,
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
                    imageBaseUrl = imageBaseUrl,
                    onPetClick = { petId -> navController.navigate(Routes.petProfile(petId)) },
                    onAddPet = { navController.navigate(Routes.ADD_PET) },
                )
            }

            composable(Routes.ADD_PET) {
                PetFormScreen(
                    title = "Add a pet",
                    submitLabel = "Add Pet",
                    errorMessage = formError,
                    onSubmit = { name, species, breed, dob, toy, gender, imageUri ->
                        viewModel.addPet(name, species, breed, dob, toy, gender, imageUri) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onClearError = viewModel::clearFormError,
                    isBusy = isBusy,
                )
            }

            composable(
                route = Routes.PET_PROFILE,
                arguments = listOf(navArgument(Routes.PET_ID_ARG) { type = NavType.StringType }),
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString(Routes.PET_ID_ARG) ?: return@composable
                val pet = pets.firstOrNull { it.petId == petId }
                val countByCategory = reminders
                    .filter { it.reminder.petId == petId }
                    .groupingBy { ReminderCategory.fromStored(it.reminder.category) }
                    .eachCount()

                PetProfileScreen(
                    pet = pet,
                    imageBaseUrl = imageBaseUrl,
                    reminderCountByCategory = countByCategory,
                    onCategoryClick = { category ->
                        navController.navigate(Routes.petCategory(petId, category.name))
                    },
                    onEditPet = { navController.navigate(Routes.editPet(petId)) },
                    onDeletePet = {
                        if (pet != null) {
                            viewModel.deletePet(pet) {
                                navController.popBackStack()
                            }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    isBusy = isBusy,
                )
            }

            composable(
                route = Routes.EDIT_PET,
                arguments = listOf(navArgument(Routes.PET_ID_ARG) { type = NavType.StringType }),
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString(Routes.PET_ID_ARG) ?: return@composable
                val pet = pets.firstOrNull { it.petId == petId } ?: return@composable

                PetFormScreen(
                    title = "Edit ${pet.name}",
                    submitLabel = "Save Changes",
                    errorMessage = formError,
                    onSubmit = { name, species, breed, dob, toy, gender, imageUri ->
                        viewModel.updatePet(pet, name, species, breed, dob, toy, gender, imageUri) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onClearError = viewModel::clearFormError,
                    initialName = pet.name,
                    initialSpecies = pet.species,
                    initialBreed = pet.breed,
                    initialDateOfBirth = pet.dateOfBirth,
                    initialFavoriteToy = pet.favoriteToy,
                    initialGender = pet.gender,
                    initialImageUrl = pet.imageUrl(imageBaseUrl),
                    isBusy = isBusy,
                )
            }

            composable(
                route = Routes.PET_CATEGORY,
                arguments = listOf(
                    navArgument(Routes.PET_ID_ARG) { type = NavType.StringType },
                    navArgument(Routes.CATEGORY_ARG) { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString(Routes.PET_ID_ARG) ?: return@composable
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
                    onDeleteReminder = viewModel::deleteReminder,
                    onAddReminder = {
                        navController.navigate(Routes.addReminder(petId, category.name))
                    },
                    onBack = { navController.popBackStack() },
                    isBusy = isBusy,
                )
            }

            composable(Routes.REMINDERS) {
                RemindersScreen(
                    reminders = reminders,
                    onCompletedChange = viewModel::setReminderCompleted,
                    onDeleteReminder = viewModel::deleteReminder,
                    onAddReminder = { navController.navigate(Routes.addReminder()) },
                    isBusy = isBusy,
                )
            }

            composable(
                route = Routes.ADD_REMINDER,
                arguments = listOf(
                    navArgument(Routes.PET_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(Routes.CATEGORY_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { backStackEntry ->
                val arguments = backStackEntry.arguments
                val preselectedPetId = arguments?.getString(Routes.PET_ID_ARG)
                    ?.takeIf { it.isNotEmpty() }
                val preselectedCategory = arguments?.getString(Routes.CATEGORY_ARG)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { ReminderCategory.fromStored(it) }

                AddReminderScreen(
                    pets = pets,
                    errorMessage = formError,
                    onSave = { petId, title, category, dueDate, dueTime, recurrence ->
                        viewModel.addReminder(petId, title, category, dueDate, dueTime, recurrence) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onClearError = viewModel::clearFormError,
                    preselectedPetId = preselectedPetId,
                    preselectedCategory = preselectedCategory,
                    isBusy = isBusy,
                )
            }
        }
    }
}

