package ua.edu.mobile.smartlife.ui.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import java.io.File

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Uri, куди камера має записати фото. rememberSaveable — щоб пережити поворот екрана
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // 1) Камера: системний застосунок робить знімок і записує його за нашим Uri
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = cameraUri
        if (success && uri != null) viewModel.onPhotoSelected(uri)
    }

    // 2) Дозвіл CAMERA: запитуємо перед запуском камери
    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = viewModel.createCameraUri()
            cameraUri = uri
            takePicture.launch(uri)
        } else {
            viewModel.showMessage("Без дозволу на камеру зробити фото неможливо")
        }
    }

    // 3) Галерея: системний Photo Picker (дозвіл на читання файлів НЕ потрібен)
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoSelected(uri)
    }

    ProfileContent(
        uiState = uiState,
        onTakePhoto = { cameraPermission.launch(Manifest.permission.CAMERA) },
        onPickPhoto = {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemovePhoto = viewModel::removePhoto,
        onMessageShown = viewModel::messageShown,
        onOpenSettings = onOpenSettings,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onMessageShown: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var showPhotoSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Показуємо повідомлення з ViewModel як Snackbar
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Профіль") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileAvatar(
                photoPath = uiState.photoPath,
                onClick = { showPhotoSheet = true }
            )
            TextButton(onClick = { showPhotoSheet = true }) { Text("Змінити фото") }
            Text(uiState.name, style = MaterialTheme.typography.headlineSmall)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ListItem(
                headlineContent = { Text(uiState.email) },
                supportingContent = { Text("Електронна пошта") },
                leadingContent = { Icon(Icons.Filled.Email, contentDescription = null) }
            )
            ListItem(
                headlineContent = { Text("Дані з сервера (GET /auth/me)") },
                supportingContent = { Text(uiState.serverInfo ?: "Завантаження...") },
                leadingContent = { Icon(Icons.Filled.Cloud, contentDescription = null) }
            )
            ListItem(
                headlineContent = { Text("Налаштування") },
                leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onOpenSettings)
            )
            ListItem(
                headlineContent = { Text("Вийти з акаунта") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onLogout)
            )
        }
    }

    if (showPhotoSheet) {
        // Нижня панель (bottom sheet) з вибором джерела фото
        ModalBottomSheet(onDismissRequest = { showPhotoSheet = false }) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                Text(
                    "Фото профілю",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Зробити фото") },
                    leadingContent = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable { showPhotoSheet = false; onTakePhoto() }
                )
                ListItem(
                    headlineContent = { Text("Обрати з галереї") },
                    leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable { showPhotoSheet = false; onPickPhoto() }
                )
                if (uiState.photoPath != null) {
                    ListItem(
                        headlineContent = { Text("Видалити фото") },
                        leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        modifier = Modifier.clickable { showPhotoSheet = false; onRemovePhoto() }
                    )
                }
            }
        }
    }
}

/** Кругле фото профілю або значок-заглушка. */
@Composable
private fun ProfileAvatar(photoPath: String?, onClick: () -> Unit) {
    val modifier = Modifier
        .size(140.dp)
        .clip(CircleShape)
        .clickable(onClick = onClick)
    if (photoPath != null) {
        // AsyncImage (бібліотека Coil) завантажує зображення у фоні та масштабує його
        AsyncImage(
            model = File(photoPath),
            contentDescription = "Фото профілю",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = modifier) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Фото профілю",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
