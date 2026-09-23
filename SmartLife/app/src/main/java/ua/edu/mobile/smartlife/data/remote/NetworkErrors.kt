package ua.edu.mobile.smartlife.data.remote

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Перетворює технічний виняток на зрозуміле користувачу повідомлення. */
fun Throwable.toUserMessage(): String = when (this) {
    is SocketTimeoutException -> "Сервер не відповідає. Спробуйте пізніше"
    is IOException -> "Немає з'єднання з інтернетом"
    is HttpException -> when (code()) {
        400 -> "Некоректний запит (400)"
        401 -> "Потрібна авторизація (401)"
        404 -> "Дані не знайдено (404)"
        in 500..599 -> "Помилка сервера (${code()})"
        else -> "Помилка HTTP ${code()}"
    }
    is SerializationException -> "Сервер повернув дані в неочікуваному форматі"
    else -> "Невідома помилка: ${message ?: javaClass.simpleName}"
}
