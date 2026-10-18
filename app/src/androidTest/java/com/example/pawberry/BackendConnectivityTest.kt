package com.example.pawberry

import com.example.pawberry.data.remote.BackendLocator
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class BackendConnectivityTest {
    @Test
    fun rootEndpointIsReachableFromEmulator() {
        val failures = mutableListOf<String>()
        for (baseUrl in BackendLocator.candidateUrls()) {
            val connection = (URL(baseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3_000
                readTimeout = 3_000
            }
            try {
                val code = connection.responseCode
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                if (code == 200 && body.contains("running", ignoreCase = true)) {
                    return
                }
                failures += "$baseUrl -> HTTP $code"
            } catch (error: Exception) {
                failures += "$baseUrl -> ${error.javaClass.simpleName}"
            } finally {
                connection.disconnect()
            }
        }
        throw AssertionError("No backend URL reachable from the emulator: ${failures.joinToString("; ")}")
    }
}
