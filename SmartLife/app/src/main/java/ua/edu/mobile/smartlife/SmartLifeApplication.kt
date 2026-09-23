package ua.edu.mobile.smartlife

import android.app.Application
import ua.edu.mobile.smartlife.di.AppContainer

/**
 * Клас Application створюється ОДИН раз при старті процесу — раніше за будь-яку Activity.
 * Тому саме тут зручно тримати контейнер залежностей.
 */
class SmartLifeApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
