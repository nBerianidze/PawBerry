package com.example.pawberry.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiConfig {
    // Preferred emulator alias for the Windows host. Newer AVDs with virtio Wi-Fi
    // often cannot reach this from the app process, so BackendLocator also tries
    // 127.0.0.1 (adb reverse) and the host LAN IPv4.
    const val PREFERRED_BASE_URL = "http://10.0.2.2:3000/"
    const val BASE_URL = PREFERRED_BASE_URL
    const val LOG_TAG = "PawBerryNet"
}

object ApiClient {
    fun create(sessionStore: SessionStore, baseUrlInterceptor: BaseUrlInterceptor): PawBerryApi {
        val authInterceptor = Interceptor { chain ->
            val token = sessionStore.token
            val request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }

        val diagnosticInterceptor = Interceptor { chain ->
            val request = chain.request()
            val started = System.nanoTime()
            try {
                val response = chain.proceed(request)
                val elapsedMs = (System.nanoTime() - started) / 1_000_000
                Log.i(
                    ApiConfig.LOG_TAG,
                    "${request.method} ${request.url.host}:${request.url.port}${request.url.encodedPath} -> ${response.code} (${elapsedMs}ms)",
                )
                response
            } catch (error: IOException) {
                val elapsedMs = (System.nanoTime() - started) / 1_000_000
                Log.e(
                    ApiConfig.LOG_TAG,
                    "${request.method} ${request.url.host}:${request.url.port}${request.url.encodedPath} failed after ${elapsedMs}ms: ${error.javaClass.simpleName}",
                )
                throw error
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(diagnosticInterceptor)
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.PREFERRED_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PawBerryApi::class.java)
    }
}
