// Кореневий файл збірки: оголошуємо плагіни, які використовує модуль :app
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
