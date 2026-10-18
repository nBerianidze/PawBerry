package com.example.pawberry.data.remote

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

class ApiException(message: String) : Exception(message)

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> message ?: "Something went wrong."
    is HttpException -> {
        val body = response()?.errorBody()?.string()
        val parsed = runCatching { Gson().fromJson(body, ApiMessage::class.java) }.getOrNull()
        parsed?.message?.takeIf { it.isNotBlank() } ?: "Request failed. Please try again."
    }
    is IOException -> message?.takeIf { it.isNotBlank() }
        ?: "Cannot reach the PawBerry server. Make sure it is running on port 3000."
    else -> "Something went wrong. Please try again."
}
