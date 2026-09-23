package ua.edu.mobile.smartlife.data.remote

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/** Перевіряємо, що кожна технічна помилка перетворюється на зрозумілий текст. */
class NetworkErrorsTest {

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun `no internet gives friendly message`() {
        assertEquals("Немає з'єднання з інтернетом", IOException("Unable to resolve host").toUserMessage())
    }

    @Test
    fun `timeout is detected before generic IO error`() {
        assertEquals("Сервер не відповідає. Спробуйте пізніше", SocketTimeoutException().toUserMessage())
    }

    @Test
    fun `http 404 and 500 are mapped`() {
        assertEquals("Дані не знайдено (404)", httpError(404).toUserMessage())
        assertEquals("Помилка сервера (503)", httpError(503).toUserMessage())
    }

    @Test
    fun `unknown error keeps its message`() {
        val message = IllegalStateException("щось пішло не так").toUserMessage()
        assertTrue(message.contains("щось пішло не так"))
    }
}
