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
import ua.edu.mobile.smartlife.data.profile.ProfilePhotoStorage
import ua.edu.mobile.smartlife.data.remote.toUserMessage
import ua.edu.mobile.smartlife.data.repository.AuthException
import ua.edu.mobile.smartlife.data.repository.AuthRepository
import ua.edu.mobile.smartlife.data.settings.SettingsRepository

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val photoPath: String? = null,
    val message: String? = null,
    val serverInfo: String? = null
)

class ProfileViewModel(
    private val settingsRepository: SettingsRepository,
    private val photoStorage: ProfilePhotoStorage,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)
    private val serverInfo = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        settingsRepository.settings, authRepository.session, message, serverInfo
    ) { settings, session, msg, info ->
        ProfileUiState(
            name = session?.fullName ?: settings.userName,
            email = session?.email.orEmpty(),
            photoPath = settings.profilePhotoPath,
            message = msg,
            serverInfo = info
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    init {
        loadServerProfile()
    }

    /** Запит GET /auth/me з токеном доступу — перевіряє, що сесія дійсна. */
    fun loadServerProfile() {
        viewModelScope.launch {
            serverInfo.value = null
            serverInfo.value = try {
                val user = authRepository.fetchProfile()
                buildString {
                    append("ID ${user.id} · @${user.username}")
                    user.age?.let { append(" · вік $it") }
                    if (user.phone.isNotBlank()) append("\nТелефон: ${user.phone}")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException) {
                e.message
            } catch (e: Exception) {
                "Не вдалося завантажити: ${e.toUserMessage()}"
            }
        }
    }

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
