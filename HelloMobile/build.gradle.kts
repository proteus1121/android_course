// Кореневий файл збірки: лише оголошуємо плагіни, які використовують модулі
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
