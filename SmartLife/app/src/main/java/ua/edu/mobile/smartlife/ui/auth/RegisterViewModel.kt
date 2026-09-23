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
import ua.edu.mobile.smartlife.data.repository.AuthRepository

data class RegisterForm(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = ""
)

data class RegisterUiState(
    val form: RegisterForm = RegisterForm(),
    val errors: Map<String, String> = emptyMap(),   // назва поля -> текст помилки
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdUserId: Long? = null                  // не null — реєстрація успішна
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFormChange(form: RegisterForm) = _uiState.update { it.copy(form = form, errors = emptyMap(), error = null) }

    fun register() {
        val form = _uiState.value.form
        val errors = validate(form)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = authRepository.register(
                    form.firstName.trim(), form.lastName.trim(), form.username.trim(),
                    form.email.trim(), form.password
                )
                _uiState.update { it.copy(isLoading = false, createdUserId = user.id) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")

        fun validate(form: RegisterForm): Map<String, String> = buildMap {
            if (form.firstName.isBlank()) put("firstName", "Введіть ім'я")
            if (form.username.trim().length < 3) put("username", "Щонайменше 3 символи")
            if (!EMAIL_REGEX.matches(form.email.trim())) put("email", "Некоректна електронна пошта")
            if (form.password.length < 8) put("password", "Щонайменше 8 символів")
            else if (form.password.none { it.isDigit() } || form.password.none { it.isLetter() }) {
                put("password", "Пароль має містити літери й цифри")
            }
        }
    }
}
