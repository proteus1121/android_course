package ua.edu.mobile.smartlife.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.remote.toUserMessage
import ua.edu.mobile.smartlife.data.repository.AuthException
import ua.edu.mobile.smartlife.data.repository.AuthRepository
import ua.edu.mobile.smartlife.data.settings.SettingsRepository

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) =
        _uiState.update { it.copy(username = value, usernameError = null, error = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, error = null) }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        // 1. Перевірка на клієнті: не надсилаємо на сервер явно некоректні дані
        val errors = validate(state.username, state.password)
        if (errors.first != null || errors.second != null) {
            _uiState.update { it.copy(usernameError = errors.first, passwordError = errors.second) }
            return
        }
        // 2. Запит до сервера
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val session = authRepository.login(state.username, state.password)
                // Ім'я з сервера використовуємо як ім'я в застосунку
                settingsRepository.setUserName(session.fullName.substringBefore(' '))
                _uiState.update { it.copy(isLoading = false, password = "") }
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    companion object {
        /** Повертає пару (помилка логіна, помилка пароля); null — поле коректне. */
        fun validate(username: String, password: String): Pair<String?, String?> {
            val usernameError = when {
                username.isBlank() -> "Введіть логін"
                username.trim().length < 3 -> "Логін має містити щонайменше 3 символи"
                else -> null
            }
            val passwordError = when {
                password.isEmpty() -> "Введіть пароль"
                password.length < 6 -> "Пароль має містити щонайменше 6 символів"
                else -> null
            }
            return usernameError to passwordError
        }
    }
}
