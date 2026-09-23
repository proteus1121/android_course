package ua.edu.mobile.smartlife.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.FakeData
import ua.edu.mobile.smartlife.data.profile.ProfilePhotoStorage
import ua.edu.mobile.smartlife.data.settings.SettingsRepository

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val photoPath: String? = null,
    val message: String? = null
)

class ProfileViewModel(
    private val settingsRepository: SettingsRepository,
    private val photoStorage: ProfilePhotoStorage
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(settingsRepository.settings, message) { settings, msg ->
        ProfileUiState(
            name = settings.userName,
            email = FakeData.userEmail,
            photoPath = settings.profilePhotoPath,
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    /** Uri файлу, куди камера запише знімок. */
    fun createCameraUri(): Uri = photoStorage.createCameraUri()

    /** Викликається, коли користувач зробив фото або обрав його в галереї. */
    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val path = photoStorage.savePhoto(uri)
                settingsRepository.setProfilePhotoPath(path)
                message.value = "Фото профілю оновлено"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message.value = "Не вдалося зберегти фото: ${e.message}"
            }
        }
    }

    fun removePhoto() {
        viewModelScope.launch {
            photoStorage.deletePhoto()
            settingsRepository.setProfilePhotoPath(null)
            message.value = "Фото видалено"
        }
    }

    fun showMessage(text: String) {
        message.value = text
    }

    fun messageShown() {
        message.value = null
    }
}
