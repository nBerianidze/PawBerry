package com.example.pawberry.data.remote

import android.util.Log
import com.example.pawberry.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class BaseUrlInterceptor : Interceptor {
    @Volatile
    var overrideUrl = ApiConfig.PREFERRED_BASE_URL.toHttpUrl()

    fun currentBaseUrl(): String = overrideUrl.toString()

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val selected = overrideUrl
        val rewritten = original.url.newBuilder()
            .scheme(selected.scheme)
            .host(selected.host)
            .port(selected.port)
            .build()
        return chain.proceed(original.newBuilder().url(rewritten).build())
    }
}

class BackendLocator(
    private val baseUrlInterceptor: BaseUrlInterceptor,
) {
    @Volatile
    private var located = false

    @Volatile
    private var lastMessage = "Backend is reachable"

    private val probeClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun locate(): String = withContext(Dispatchers.IO) {
        if (located) {
            return@withContext lastMessage
        }
        val attempts = linkedMapOf<String, String>()
        for (baseUrl in candidateUrls()) {
            try {
                val request = Request.Builder().url(baseUrl).get().build()
                probeClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        baseUrlInterceptor.overrideUrl = baseUrl.toHttpUrl()
                        val parsed = runCatching {
                            Gson().fromJson(body, ApiMessage::class.java)
                        }.getOrNull()?.message
                        val message = parsed?.takeIf { it.isNotBlank() }
                            ?: body.takeIf { it.isNotBlank() }
                            ?: "Backend is reachable"
                        lastMessage = message
                        located = true
                        Log.i(ApiConfig.LOG_TAG, "Using backend $baseUrl")
                        return@withContext message
                    }
                    attempts[baseUrl] = "HTTP ${response.code}"
                }
            } catch (error: IOException) {
                attempts[baseUrl] = error.javaClass.simpleName
                Log.w(
                    ApiConfig.LOG_TAG,
                    "Backend probe failed for $baseUrl: ${error.javaClass.simpleName}",
                )
            }
        }

        val detail = attempts.entries.joinToString("; ") { "${it.key} -> ${it.value}" }
        Log.e(ApiConfig.LOG_TAG, "No backend URL reachable. $detail")
        throw IOException(
            "Cannot reach the PawBerry server on port 3000. Tried: $detail"
        )
    }

    companion object {
        fun candidateUrls(): List<String> = buildList {
            add(ApiConfig.PREFERRED_BASE_URL)
            val hostIp = BuildConfig.HOST_IPV4
            if (hostIp.isNotBlank()) {
                add("http://$hostIp:3000/")
            }
            add("http://127.0.0.1:3000/")
            add("http://localhost:3000/")
        }.distinct()
    }
}
