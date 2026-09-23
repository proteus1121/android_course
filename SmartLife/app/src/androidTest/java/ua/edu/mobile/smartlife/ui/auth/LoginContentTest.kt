package ua.edu.mobile.smartlife.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ua.edu.mobile.smartlife.ui.theme.SmartLifeTheme

/**
 * UI-тест екрана входу. Тестуємо stateless-частину (LoginContent),
 * тому не потрібні ні мережа, ні ViewModel.
 */
@RunWith(AndroidJUnit4::class)
class LoginContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun typingUpdatesUsernameCallback() {
        var typed = ""
        composeTestRule.setContent {
            SmartLifeTheme {
                LoginContent(
                    uiState = LoginUiState(),
                    onUsernameChange = { typed = it },
                    onPasswordChange = {},
                    onLoginClick = {},
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("username").performTextInput("emilys")

        assertEquals("emilys", typed)
    }

    @Test
    fun loginButtonCallsCallback() {
        var clicked = false
        composeTestRule.setContent {
            SmartLifeTheme {
                LoginContent(
                    uiState = LoginUiState(username = "emilys", password = "emilyspass"),
                    onUsernameChange = {}, onPasswordChange = {},
                    onLoginClick = { clicked = true },
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Увійти").performClick()

        assertTrue(clicked)
    }

    @Test
    fun serverErrorIsDisplayed() {
        composeTestRule.setContent {
            SmartLifeTheme {
                LoginContent(
                    uiState = LoginUiState(error = "Неправильний логін або пароль"),
                    onUsernameChange = {}, onPasswordChange = {}, onLoginClick = {}, onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("loginError").assertIsDisplayed()
        composeTestRule.onNodeWithText("Неправильний логін або пароль").assertIsDisplayed()
    }

    @Test
    fun validationErrorsAreDisplayedUnderFields() {
        composeTestRule.setContent {
            SmartLifeTheme {
                LoginContent(
                    uiState = LoginUiState(usernameError = "Введіть логін", passwordError = "Введіть пароль"),
                    onUsernameChange = {}, onPasswordChange = {}, onLoginClick = {}, onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Введіть логін").assertIsDisplayed()
        composeTestRule.onNodeWithText("Введіть пароль").assertIsDisplayed()
    }
}
