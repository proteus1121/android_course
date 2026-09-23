package ua.edu.mobile.smartlife.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ua.edu.mobile.smartlife.data.FakeData
import ua.edu.mobile.smartlife.data.settings.SettingsRepository

data class ProfileUiState(
    val name: String = "",
    val email: String = ""
)

class ProfileViewModel(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = settingsRepository.settings
        .map { ProfileUiState(name = it.userName, email = FakeData.userEmail) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}
