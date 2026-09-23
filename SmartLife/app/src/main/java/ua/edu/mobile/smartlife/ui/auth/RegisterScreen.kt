package ua.edu.mobile.smartlife.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.edu.mobile.smartlife.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form = uiState.form

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Реєстрація") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormField("Ім'я", form.firstName, uiState.errors["firstName"]) {
                viewModel.onFormChange(form.copy(firstName = it))
            }
            FormField("Прізвище", form.lastName, uiState.errors["lastName"]) {
                viewModel.onFormChange(form.copy(lastName = it))
            }
            FormField("Логін", form.username, uiState.errors["username"]) {
                viewModel.onFormChange(form.copy(username = it))
            }
            FormField("Електронна пошта", form.email, uiState.errors["email"], KeyboardType.Email) {
                viewModel.onFormChange(form.copy(email = it))
            }
            FormField("Пароль", form.password, uiState.errors["password"], KeyboardType.Password, isPassword = true) {
                viewModel.onFormChange(form.copy(password = it))
            }
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = viewModel::register,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                else Text("Створити акаунт")
            }
        }
    }

    uiState.createdUserId?.let { id ->
        AlertDialog(
            onDismissRequest = onBack,
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
            title = { Text("Запит на реєстрацію успішний") },
            text = {
                Text(
                    "Сервер повернув користувача з id = $id.\n\n" +
                        "Зверніть увагу: DummyJSON — навчальний сервер, він лише імітує створення " +
                        "і не зберігає нових користувачів. Для входу використайте тестовий акаунт " +
                        "emilys / emilyspass."
                )
            },
            confirmButton = { TextButton(onClick = onBack) { Text("До входу") } }
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation()
        else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}
