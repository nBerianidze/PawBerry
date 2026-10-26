package com.example.pawberry.util

import java.net.IDN

/**
 * Format checks for the registration form.
 * The server repeats this check and also looks up MX records. Neither check
 * proves that an individual mailbox exists.
 */
object EmailValidator {
    const val REQUIRED_MESSAGE = "Email is required."
    const val FORMAT_MESSAGE = "Enter a valid email address, like name@example.com."

    private const val MAX_EMAIL_LENGTH = 320
    private val LOCAL_PART = Regex(
        "^[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+)*$",
    )
    private val DNS_LABEL = Regex("^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$")
    private val LETTER_TLD = Regex("^[a-z]{2,}$")
    private val PUNYCODE_TLD = Regex("^xn--[a-z0-9-]{2,}$")
    private val IPV4 = Regex("""^(?:\d{1,3}\.){3}\d{1,3}$""")

    /** Full format check used on submit. Blank input is its own message. */
    fun formatError(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return REQUIRED_MESSAGE
        if (!isReasonableEmail(trimmed)) return FORMAT_MESSAGE
        return null
    }

    /**
     * Feedback while typing. Incomplete addresses such as "ada" or "ada@gm"
     * stay quiet until they look finished or are clearly malformed.
     */
    fun liveFormatError(raw: String): String? {
        val error = formatError(raw) ?: return null
        if (raw.isBlank() || !shouldWarnWhileTyping(raw)) return null
        return error
    }

    private fun isReasonableEmail(trimmed: String): Boolean {
        if (trimmed.length > MAX_EMAIL_LENGTH) return false
        val at = trimmed.indexOf('@')
        if (at <= 0 || at != trimmed.lastIndexOf('@') || at == trimmed.length - 1) {
            return false
        }
        val local = trimmed.substring(0, at)
        val domain = trimmed.substring(at + 1)
        return isValidLocalPart(local) && toAsciiDomain(domain) != null
    }

    private fun shouldWarnWhileTyping(raw: String): Boolean {
        val value = raw.trim()
        if (value.any { it.isWhitespace() }) return true
        val atCount = value.count { it == '@' }
        if (atCount == 0) return false
        if (atCount > 1 || value.startsWith("@")) return true
        val domain = value.substringAfter('@')
        if (domain.isEmpty() || domain.endsWith('.')) return false
        if (domain.startsWith('.') || domain.contains("..") || domain.contains('_')) return true
        if (!domain.contains('.')) return false
        val tld = domain.substringAfterLast('.')
        if (tld.length < 2) return false
        return true
    }

    private fun isValidLocalPart(local: String): Boolean {
        return local.length <= 64 && LOCAL_PART.matches(local)
    }

    private fun toAsciiDomain(domain: String): String? {
        if (
            domain.length > 253 ||
            domain.contains("..") ||
            domain.startsWith(".") ||
            domain.endsWith(".") ||
            domain.any { it.isWhitespace() || it == '@' || it == '_' }
        ) {
            return null
        }

        val ascii = try {
            IDN.toASCII(domain, IDN.ALLOW_UNASSIGNED).lowercase()
        } catch (_: IllegalArgumentException) {
            return null
        }
        if (ascii.isEmpty() || ascii.length > 253 || isIpAddress(ascii) || !isDnsHostname(ascii)) {
            return null
        }
        return ascii
    }

    private fun isDnsHostname(hostname: String): Boolean {
        val labels = hostname.split('.')
        if (labels.size < 2 || labels.any { !DNS_LABEL.matches(it) }) return false
        val tld = labels.last()
        return LETTER_TLD.matches(tld) || PUNYCODE_TLD.matches(tld)
    }

    private fun isIpAddress(host: String): Boolean {
        return IPV4.matches(host) || host.contains(':')
    }
}
