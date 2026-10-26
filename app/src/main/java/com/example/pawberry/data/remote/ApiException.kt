package com.example.pawberry.data.remote

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

class ApiException(
    message: String,
    val field: String? = null,
    val code: String? = null,
) : Exception(message)

data class ApiError(
    val message: String,
    val field: String? = null,
    val code: String? = null,
)

fun Throwable.toApiError(): ApiError = when (this) {
    is ApiException -> ApiError(
        message = message ?: "Something went wrong.",
        field = field,
        code = code,
    )
    is HttpException -> {
        val body = response()?.errorBody()?.string()
        val parsed = runCatching { Gson().fromJson(body, ApiMessage::class.java) }.getOrNull()
        ApiError(
            message = parsed?.message?.takeIf { it.isNotBlank() } ?: "Request failed. Please try again.",
            field = parsed?.field,
            code = parsed?.code,
        )
    }
    is IOException -> ApiError(
        message = message?.takeIf { it.isNotBlank() }
            ?: "Cannot reach the PawBerry server. Make sure it is running on port 3000.",
    )
    else -> ApiError(message = "Something went wrong. Please try again.")
}

fun Throwable.toUserMessage(): String = toApiError().message
