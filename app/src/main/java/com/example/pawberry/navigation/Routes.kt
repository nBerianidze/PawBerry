package com.example.pawberry.navigation

import com.example.pawberry.R

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val MY_PETS = "my_pets"
    const val ADD_PET = "add_pet"
    const val REMINDERS = "reminders"

    const val PET_ID_ARG = "petId"
    const val CATEGORY_ARG = "category"

    /** Pet detail, e.g. `pet_profile/4`. */
    const val PET_PROFILE = "pet_profile/{$PET_ID_ARG}"
    fun petProfile(petId: Long) = "pet_profile/$petId"

    /** Edit form for an existing pet, e.g. `edit_pet/4`. */
    const val EDIT_PET = "edit_pet/{$PET_ID_ARG}"
    fun editPet(petId: Long) = "edit_pet/$petId"

    /**
     * Treatment history for one category of one pet, e.g. `pet_category/4/VACCINATION`.
     * The category travels as the enum name so it is always URL safe.
     */
    const val PET_CATEGORY = "pet_category/{$PET_ID_ARG}/{$CATEGORY_ARG}"
    fun petCategory(petId: Long, category: String) = "pet_category/$petId/$category"

    /**
     * New reminder form. Both arguments are optional: opening it from a pet's category
     * screen pre-selects that pet and category, opening it from Reminders does not.
     */
    const val ADD_REMINDER = "add_reminder?$PET_ID_ARG={$PET_ID_ARG}&$CATEGORY_ARG={$CATEGORY_ARG}"
    fun addReminder(petId: Long? = null, category: String? = null): String =
        if (petId == null || category == null) {
            "add_reminder"
        } else {
            "add_reminder?$PET_ID_ARG=$petId&$CATEGORY_ARG=$category"
        }
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
