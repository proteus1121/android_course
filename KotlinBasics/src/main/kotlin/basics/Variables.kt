package basics

// Тема 2.1–2.2. Змінні та типи даних
fun variablesDemo() {
    val appName = "Smart Life"   // val — значення не можна змінити (read-only)
    var steps = 4200             // var — значення можна змінювати
    steps += 800                 // steps тепер 5000

    // Явне зазначення типу (зазвичай Kotlin виводить тип сам)
    val waterLiters: Double = 1.5
    val isGoalReached: Boolean = steps >= 5000
    val firstLetter: Char = appName[0]
    val sleepMinutes: Long = 450L

    println("Застосунок: $appName")
    println("Кроки: $steps, вода: $waterLiters л, ціль досягнута: $isGoalReached")
    println("Перша літера: $firstLetter, сон: ${sleepMinutes / 60} год ${sleepMinutes % 60} хв")

    // Перетворення типів виконується явно
    val stepsText = "7300"
    val stepsNumber = stepsText.toInt()
    println("Рядок у число: ${stepsNumber + 100}")
}
