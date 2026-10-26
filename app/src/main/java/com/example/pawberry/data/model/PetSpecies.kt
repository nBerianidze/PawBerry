package com.example.pawberry.data.model

object PetSpecies {
    const val OTHER = "Other"

    private val catalog = listOf(
        SpeciesBreeds(
            "Dog",
            listOf(
                "Australian Shepherd",
                "Beagle",
                "Boxer",
                "Bulldog",
                "Cavalier King Charles Spaniel",
                "Chihuahua",
                "Corgi",
                "Dachshund",
                "Doberman",
                "French Bulldog",
                "German Shepherd",
                "Golden Retriever",
                "Great Dane",
                "Labrador Retriever",
                "Mixed",
                "Pomeranian",
                "Poodle",
                "Rottweiler",
                "Shih Tzu",
                "Siberian Husky",
                "Yorkshire Terrier",
            ),
        ),
        SpeciesBreeds(
            "Cat",
            listOf(
                "Abyssinian",
                "American Shorthair",
                "Bengal",
                "British Shorthair",
                "Domestic Longhair",
                "Domestic Shorthair",
                "Maine Coon",
                "Mixed",
                "Persian",
                "Ragdoll",
                "Russian Blue",
                "Scottish Fold",
                "Siamese",
                "Sphynx",
            ),
        ),
        SpeciesBreeds(
            "Rabbit",
            listOf(
                "Dutch",
                "Flemish Giant",
                "Holland Lop",
                "Lionhead",
                "Mini Lop",
                "Mini Rex",
                "Mixed",
                "Netherland Dwarf",
                "Rex",
            ),
        ),
        SpeciesBreeds(
            "Guinea Pig",
            listOf("Abyssinian", "American", "Mixed", "Peruvian", "Silkie", "Skinny", "Teddy"),
        ),
        SpeciesBreeds(
            "Hamster",
            listOf("Campbell's Dwarf", "Chinese", "Roborovski", "Syrian", "Winter White"),
        ),
        SpeciesBreeds("Gerbil", listOf("Fat-tailed", "Mongolian")),
        SpeciesBreeds("Mouse", listOf("Fancy Mouse")),
        SpeciesBreeds("Rat", listOf("Dumbo", "Standard")),
        SpeciesBreeds("Ferret", listOf("Albino", "Angora", "Sable")),
        SpeciesBreeds("Chinchilla", listOf("Beige", "Ebony", "Standard Gray", "White")),
        SpeciesBreeds("Hedgehog", listOf("African Pygmy")),
        SpeciesBreeds(
            "Parrot",
            listOf(
                "African Grey",
                "Amazon",
                "Cockatoo",
                "Conure",
                "Eclectus",
                "Macaw",
                "Quaker",
                "Senegal",
            ),
        ),
        SpeciesBreeds("Parakeet", listOf("American", "English")),
        SpeciesBreeds("Canary", listOf("American Singer", "Border", "German Roller", "Gloster")),
        SpeciesBreeds(
            "Cockatiel",
            listOf("Cinnamon", "Grey", "Lutino", "Pearl", "Pied", "Whiteface"),
        ),
        SpeciesBreeds("Finch", listOf("Gouldian", "Owl", "Society", "Zebra")),
        SpeciesBreeds("Lovebird", listOf("Fischer's", "Masked", "Peach-faced")),
        SpeciesBreeds("Goldfish", listOf("Comet", "Common", "Fantail", "Oranda", "Ryukin")),
        SpeciesBreeds("Betta Fish", listOf("Crowntail", "Halfmoon", "Plakat", "Veiltail")),
        SpeciesBreeds("Turtle", listOf("Box", "Musk", "Painted", "Red-eared Slider")),
        SpeciesBreeds("Tortoise", listOf("Greek", "Hermann's", "Red-footed", "Russian", "Sulcata")),
        SpeciesBreeds(
            "Horse",
            listOf("Appaloosa", "Arabian", "Mustang", "Paint", "Quarter Horse", "Thoroughbred"),
        ),
    )

    val predefined: List<String> = catalog.map { it.species } + OTHER

    fun displayList(): List<String> = sortedNames(catalog.map { it.species }) + OTHER

    fun canonical(value: String): String? =
        catalog.firstOrNull { it.species.equals(value, ignoreCase = true) }?.species

    fun breedsFor(species: String): List<String> {
        val entry = catalog.firstOrNull { it.species.equals(species, ignoreCase = true) }
            ?: return emptyList()
        return sortedNames(entry.breeds) + OTHER
    }

    fun canonicalBreed(species: String, breed: String): String? =
        breedsFor(species).firstOrNull { it.equals(breed, ignoreCase = true) && it != OTHER }

    fun isPredefined(value: String): Boolean = canonical(value) != null

    private fun sortedNames(names: List<String>): List<String> =
        names.distinct().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })

    private data class SpeciesBreeds(
        val species: String,
        val breeds: List<String>,
    )
}
