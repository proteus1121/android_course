package ua.edu.mobile.smartlife.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Тестовий backend DummyJSON (https://dummyjson.com/docs/auth).
 * Тестовий користувач: emilys / emilyspass.
 */
interface AuthApi {

    /** POST /auth/login — вхід; у відповідь сервер видає accessToken і refreshToken. */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /** GET /auth/me — дані поточного користувача. Потребує заголовка Authorization: Bearer <token>. */
    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): UserDto

    /** POST /auth/refresh — отримати нову пару токенів за refreshToken. */
    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse

    /** POST /users/add — реєстрація (DummyJSON лише імітує створення, користувач не зберігається). */
    @POST("users/add")
    suspend fun register(@Body request: RegisterRequest): UserDto
}

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val expiresInMins: Int = 30
)

@Serializable
data class LoginResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val image: String = "",
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
    val expiresInMins: Int = 30
)

@Serializable
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val age: Int? = null,
    val phone: String = "",
    val image: String = ""
)
