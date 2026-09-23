package basics

// Тема 2.11. Базова робота з об'єктами

// object — єдиний екземпляр (singleton) на весь застосунок
object AppConfig {
    const val BASE_URL = "https://api.open-meteo.com/"
    var isDarkTheme = false
}

// companion object — "статичні" члени класу
class Measurement private constructor(val value: Double) {
    companion object {
        fun fromCelsius(value: Double) = Measurement(value)
    }
}

// Інтерфейс описує, ЩО вміє об'єкт, а клас — ЯК саме
interface Sensor {
    val name: String
    fun read(): Float
}

class FakeLightSensor : Sensor {
    override val name = "Датчик освітленості"
    override fun read(): Float = 320f
}

// enum — фіксований набір значень
enum class Mood(val emoji: String) { BAD("🙁"), OK("😐"), GOOD("🙂") }

// sealed interface — закритий набір станів (знадобиться для UI: завантаження/помилка/успіх)
sealed interface LoadState {
    data object Loading : LoadState
    data class Success(val data: String) : LoadState
    data class Error(val message: String) : LoadState
}

fun render(state: LoadState): String = when (state) {
    LoadState.Loading -> "Завантаження..."
    is LoadState.Success -> "Дані: ${state.data}"
    is LoadState.Error -> "Помилка: ${state.message}"
}

// Функція-розширення: додаємо метод до існуючого класу String
fun String.capitalizeFirst(): String = replaceFirstChar { it.uppercase() }

fun objectsDemo() {
    AppConfig.isDarkTheme = true
    println("URL: ${AppConfig.BASE_URL}, темна тема: ${AppConfig.isDarkTheme}")
    println("Температура: ${Measurement.fromCelsius(21.5).value}")

    val sensor: Sensor = FakeLightSensor()
    println("${sensor.name}: ${sensor.read()} lx")

    println("Настрій: ${Mood.GOOD} ${Mood.GOOD.emoji}, усього варіантів: ${Mood.entries.size}")

    listOf(LoadState.Loading, LoadState.Success("18 °C"), LoadState.Error("Немає мережі"))
        .forEach { println(render(it)) }

    println("smart life".capitalizeFirst())

    // apply — налаштувати об'єкт і повернути його
    val profile = UserProfile("Андрій", 20).apply { dailyStepGoal = 12_000 }
    println(profile.introduce())
}
