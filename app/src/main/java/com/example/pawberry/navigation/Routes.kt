package com.example.pawberry.navigation

import com.example.pawberry.R

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val MY_PETS = "my_pets"
    const val ADD_PET = "add_pet"
    const val REMINDERS = "reminders"
}

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: Int,
) {
    HOME(Routes.HOME, "Home", R.drawable.ic_home),
    MY_PETS(Routes.MY_PETS, "My Pets", R.drawable.ic_pets),
    REMINDERS(Routes.REMINDERS, "Reminders", R.drawable.ic_reminders),
}
