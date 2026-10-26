package com.example.pawberry

import com.example.pawberry.data.model.PetSpecies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PetSpeciesTest {
    @Test
    fun speciesListKeepsDomesticAnimalsAndOtherLast() {
        val species = PetSpecies.displayList()
        assertEquals(PetSpecies.OTHER, species.last())
        assertTrue(species.containsAll(listOf("Dog", "Cat", "Parrot", "Rabbit", "Horse")))
        assertFalse(species.contains("Snake"))
        assertFalse(species.contains("Alpaca"))
        assertFalse(species.contains("Axolotl"))
    }

    @Test
    fun breedsFollowTheSelectedSpeciesAndEndWithOther() {
        val dogBreeds = PetSpecies.breedsFor("dog")
        assertEquals(PetSpecies.OTHER, dogBreeds.last())
        assertTrue(dogBreeds.contains("Labrador Retriever"))
        assertFalse(dogBreeds.contains("Siamese"))

        val catBreeds = PetSpecies.breedsFor("Cat")
        assertTrue(catBreeds.contains("Siamese"))
        assertFalse(catBreeds.contains("Labrador Retriever"))
        assertTrue(PetSpecies.breedsFor("Dragon").isEmpty())
    }

    @Test
    fun savedCustomValuesStayOutOfThePredefinedLists() {
        assertEquals("Dog", PetSpecies.canonical("dog"))
        assertEquals("Labrador Retriever", PetSpecies.canonicalBreed("Dog", "labrador retriever"))
        assertNull(PetSpecies.canonical("Iguana"))
        assertNull(PetSpecies.canonicalBreed("Dog", "Village mix"))
    }
}