package ua.edu.mobile.smartlife.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.repository.AuthRepository

/** Стан сесії для всього застосунку: чи увійшов користувач. */
class SessionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** null — ще читаємо сесію з диска; true/false — відомо, чи є вхід. */
    val isLoggedIn: StateFlow<Boolean?> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
