package ua.edu.mobile.smartlife.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import ua.edu.mobile.smartlife.data.auth.Session
import ua.edu.mobile.smartlife.data.auth.SessionStorage
import ua.edu.mobile.smartlife.data.remote.AuthApi
import ua.edu.mobile.smartlife.data.remote.LoginRequest
import ua.edu.mobile.smartlife.data.remote.RefreshRequest
import ua.edu.mobile.smartlife.data.remote.RegisterRequest
import ua.edu.mobile.smartlife.data.remote.UserDto

/** Помилка, яку можна показати користувачу без змін. */
class AuthException(message: String) : Exception(message)

class AuthRepository(
    private val api: AuthApi,
    private val sessionStorage: SessionStorage
) {
    val session: Flow<Session?> = sessionStorage.session

    val isLoggedIn: Flow<Boolean> = session.map { it != null }

    /** Вхід: надсилаємо логін і пароль, у відповідь отримуємо токени і зберігаємо сесію. */
    suspend fun login(username: String, password: String): Session {
        val response = try {
            api.login(LoginRequest(username.trim(), password))
        } catch (e: HttpException) {
            // DummyJSON повертає 400 для неправильних облікових даних
            if (e.code() == 400 || e.code() == 401) throw AuthException("Неправильний логін або пароль")
            throw e
        }
        val session = Session(
            userId = response.id,
            username = response.username,
            fullName = "${response.firstName} ${response.lastName}".trim(),
            email = response.email,
            accessToken = response.accessToken,
            refreshToken = response.refreshToken
        )
        sessionStorage.save(session)
        return session
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String
    ): UserDto = api.register(RegisterRequest(firstName, lastName, username, email, password))

    /**
     * Профіль з сервера. Якщо accessToken прострочений (401),
     * пробуємо один раз оновити його через refreshToken і повторити запит.
     */
    suspend fun fetchProfile(): UserDto {
        val current = sessionStorage.session.first() ?: throw AuthException("Сесію не знайдено")
        return try {
            api.me(bearer(current.accessToken))
        } catch (e: HttpException) {
            if (e.code() != 401) throw e
            val tokens = try {
                api.refresh(RefreshRequest(current.refreshToken))
            } catch (refreshError: HttpException) {
                logout() // refreshToken теж недійсний — потрібно увійти знову
                throw AuthException("Сесія завершилася. Увійдіть знову")
            }
            sessionStorage.updateTokens(tokens.accessToken, tokens.refreshToken)
            api.me(bearer(tokens.accessToken))
        }
    }

    suspend fun logout() {
        sessionStorage.clear()
    }

    private fun bearer(token: String) = "Bearer $token"
}
