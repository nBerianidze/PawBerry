package com.example.pawberry.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface PawBerryApi {
    @GET("/")
    suspend fun healthCheck(): ApiMessage

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/users/me")
    suspend fun getMe(): UserDto

    @GET("api/pets")
    suspend fun getPets(): List<PetDto>

    @POST("api/pets")
    suspend fun createPet(@Body body: CreatePetRequest): PetDto

    @PUT("api/pets/{id}")
    suspend fun updatePet(@Path("id") id: String, @Body body: UpdatePetRequest): PetDto

    @DELETE("api/pets/{id}")
    suspend fun deletePet(@Path("id") id: String): ApiMessage

    @Multipart
    @POST("api/uploads")
    suspend fun uploadImage(@Part image: MultipartBody.Part): UploadResponse

    @GET("api/reminders")
    suspend fun getReminders(): List<ReminderDto>

    @POST("api/reminders")
    suspend fun createReminder(@Body body: CreateReminderRequest): ReminderDto

    @PUT("api/reminders/{id}")
    suspend fun updateReminder(@Path("id") id: String, @Body body: UpdateReminderRequest): ReminderDto

    @PATCH("api/reminders/{id}/complete")
    suspend fun completeReminder(@Path("id") id: String): ReminderDto

    @DELETE("api/reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: String): ApiMessage

    @GET("api/rewards")
    suspend fun getRewards(): RewardDto
}
