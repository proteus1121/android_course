package ua.edu.mobile.smartlife.data.profile

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Робота з файлами фото профілю.
 * - Камера записує знімок у тимчасовий файл, доступ до якого ми даємо через FileProvider.
 * - Обране фото (з камери чи галереї) копіюємо у внутрішню пам'ять застосунку (filesDir).
 */
class ProfilePhotoStorage(private val context: Context) {

    /** Створює порожній файл у cache/images і повертає content:// Uri для камери. */
    fun createCameraUri(): Uri {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
        // authority має збігатися з android:authorities у AndroidManifest.xml
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Копіює зображення за Uri у файл застосунку й повертає шлях до нього.
     * Робота з диском виконується у фоновому потоці (Dispatchers.IO).
     */
    suspend fun savePhoto(source: Uri): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "profile").apply { mkdirs() }
        // Нове ім'я при кожній зміні — щоб бібліотека зображень не показала старе фото з кешу
        val target = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Не вдалося прочитати зображення")
        // Видаляємо попередні фото, щоб не займати місце
        dir.listFiles()?.filter { it != target }?.forEach { it.delete() }
        target.absolutePath
    }

    suspend fun deletePhoto() = withContext(Dispatchers.IO) {
        File(context.filesDir, "profile").deleteRecursively()
    }
}
