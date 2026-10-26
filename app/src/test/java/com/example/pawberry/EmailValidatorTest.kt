package com.example.pawberry

import com.example.pawberry.util.EmailValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmailValidatorTest {
    @Test
    fun acceptsReasonableAddresses() {
        listOf(
            "ada@example.com",
            "user.name+tag@example.co.uk",
            "User@Example.COM",
            "  user@example.com  ",
            "user@bücher.de",
            "user@xn--bcher-kva.de",
        ).forEach { email ->
            assertNull(email, EmailValidator.formatError(email))
        }
    }

    @Test
    fun rejectsEmptyAndMalformedAddresses() {
        val format = EmailValidator.FORMAT_MESSAGE
        val cases = mapOf(
            "" to EmailValidator.REQUIRED_MESSAGE,
            "   " to EmailValidator.REQUIRED_MESSAGE,
            "userexample.com" to format,
            "@example.com" to format,
            "user@" to format,
            "user@@example.com" to format,
            "user@example" to format,
            "user@.example.com" to format,
            "user@example..com" to format,
            "user@example." to format,
            "user@-example.com" to format,
            "user@example-.com" to format,
            "user@example.c" to format,
            "user@1.2.3.4" to format,
            "user name@example.com" to format,
            ".user@example.com" to format,
            "user.@example.com" to format,
            "user..name@example.com" to format,
            "user@exam_ple.com" to format,
            "a".repeat(65) + "@example.com" to format,
        )
        cases.forEach { (email, expected) ->
            assertEquals(email, expected, EmailValidator.formatError(email))
        }
    }

    @Test
    fun liveFeedbackWaitsUntilTheAddressLooksFinishedOrObviouslyWrong() {
        assertNull(EmailValidator.liveFormatError(""))
        assertNull(EmailValidator.liveFormatError("ada"))
        assertNull(EmailValidator.liveFormatError("ada@"))
        assertNull(EmailValidator.liveFormatError("ada@gmail"))
        assertNull(EmailValidator.liveFormatError("ada@gmail."))
        assertNull(EmailValidator.liveFormatError("ada@gmail.c"))
        assertNull(EmailValidator.liveFormatError("ada@gmail.com"))

        assertEquals(
            EmailValidator.FORMAT_MESSAGE,
            EmailValidator.liveFormatError("ada@@gmail.com"),
        )
        assertEquals(
            EmailValidator.FORMAT_MESSAGE,
            EmailValidator.liveFormatError("@gmail.com"),
        )
        assertEquals(
            EmailValidator.FORMAT_MESSAGE,
            EmailValidator.liveFormatError("ada @gmail.com"),
        )
        assertEquals(
            EmailValidator.FORMAT_MESSAGE,
            EmailValidator.liveFormatError("ada@gmail..com"),
        )
        assertEquals(
            EmailValidator.FORMAT_MESSAGE,
            EmailValidator.liveFormatError("not an email"),
        )
    }
}
