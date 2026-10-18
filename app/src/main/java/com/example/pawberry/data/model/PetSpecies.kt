package com.example.pawberry.data.model

object PetSpecies {
    const val OTHER = "Other"

    val predefined: List<String> = listOf(
        "Alpaca",
        "Axolotl",
        "Betta Fish",
        "Bird",
        "Budgerigar",
        "Canary",
        "Cat",
        "Chicken",
        "Chinchilla",
        "Cockatiel",
        "Cockatoo",
        "Cow",
        "Crab",
        "Degus",
        "Dog",
        "Donkey",
        "Duck",
        "Ferret",
        "Finch",
        "Fish",
        "Frog",
        "Gerbil",
        "Goat",
        "Goldfish",
        "Goose",
        "Guinea Fowl",
        "Guinea Pig",
        "Hamster",
        "Hedgehog",
        "Hermit Crab",
        "Horse",
        "Iguana",
        "Koi",
        "Llama",
        "Lovebird",
        "Lizard",
        "Macaw",
        "Mouse",
        "Newt",
        "Parakeet",
        "Parrot",
        "Pig",
        "Pigeon",
        "Quail",
        "Rabbit",
        "Rat",
        "Salamander",
        "Sheep",
        "Snake",
        "Sugar Glider",
        "Toad",
        "Tortoise",
        "Turkey",
        "Turtle",
        OTHER,
    )

    fun displayList(): List<String> =
        predefined.filter { it != OTHER }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }) + OTHER

    fun isPredefined(value: String): Boolean =
        predefined.any { it.equals(value, ignoreCase = true) && !it.equals(OTHER, ignoreCase = true) }
}
