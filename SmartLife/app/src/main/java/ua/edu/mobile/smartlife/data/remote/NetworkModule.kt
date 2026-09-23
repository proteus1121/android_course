package ua.edu.mobile.smartlife.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ua.edu.mobile.smartlife.BuildConfig
import java.util.concurrent.TimeUnit

/** Налаштування мережевого шару: HTTP-клієнт, JSON-парсер і фабрика Retrofit. */
object NetworkModule {

    // ignoreUnknownKeys — не падати, якщо сервер повернув поля, яких немає в DTO
    val json = Json { ignoreUnknownKeys = true }

    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            // Логуємо запити лише в debug-збірці (у Logcat з тегом okhttp.OkHttpClient)
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
            // Токен не повинен потрапляти в логи навіть у debug-збірці
            redactHeader("Authorization")
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun createRetrofit(baseUrl: String, client: OkHttpClient = okHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl) // обов'язково закінчується на "/"
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
