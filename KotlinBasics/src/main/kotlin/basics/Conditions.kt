package basics

// Тема 2.3. Умови if та when
fun describeSteps(steps: Int): String {
    // if у Kotlin — це вираз: він повертає значення
    return if (steps >= 10_000) {
        "Чудова активність!"
    } else if (steps >= 5_000) {
        "Непогано, але можна більше"
    } else {
        "Час прогулятися"
    }
}

fun moodEmoji(mood: Int): String = when (mood) {
    1 -> "😞"
    2 -> "🙁"
    3 -> "😐"
    4 -> "🙂"
    5 -> "😄"
    else -> "?"
}

fun temperatureLevel(celsius: Double): String = when {
    celsius < 0 -> "мороз"
    celsius in 0.0..15.0 -> "прохолодно"
    celsius in 15.0..25.0 -> "комфортно"
    else -> "спекотно"
}

fun conditionsDemo() {
    println(describeSteps(12_000))
    println(describeSteps(3_000))
    println("Настрій 4 -> ${moodEmoji(4)}")
    println("18.5 °C -> ${temperatureLevel(18.5)}")
}
