package ua.edu.mobile.smartlife.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun `empty login form returns both errors`() {
        val (loginError, passwordError) = LoginViewModel.validate("", "")
        assertEquals("Введіть логін", loginError)
        assertEquals("Введіть пароль", passwordError)
    }

    @Test
    fun `short password is rejected`() {
        val (loginError, passwordError) = LoginViewModel.validate("emilys", "123")
        assertNull(loginError)
        assertEquals("Пароль має містити щонайменше 6 символів", passwordError)
    }

    @Test
    fun `valid login form has no errors`() {
        assertEquals(null to null, LoginViewModel.validate("emilys", "emilyspass"))
    }

    @Test
    fun `register form checks email and password strength`() {
        val errors = RegisterViewModel.validate(
            RegisterForm(firstName = "Olena", username = "olenak", email = "olena@", password = "onlyletters")
        )
        assertEquals(setOf("email", "password"), errors.keys)
        assertEquals("Пароль має містити літери й цифри", errors["password"])
    }

    @Test
    fun `correct register form passes`() {
        val errors = RegisterViewModel.validate(
            RegisterForm("Olena", "Koval", "olenak", "olena@example.com", "secret2026")
        )
        assertTrue(errors.isEmpty())
    }
}
