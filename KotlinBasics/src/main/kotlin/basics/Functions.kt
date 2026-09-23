package basics

// Тема 2.5. Функції
fun bmi(weightKg: Double, heightM: Double): Double {
    return weightKg / (heightM * heightM)
}

// Функція-вираз (одним рядком) та параметр зі значенням за замовчуванням
fun greet(name: String, greeting: String = "Привіт") = "$greeting, $name!"

// Функція без результату повертає Unit (аналог void)
fun printSeparator(length: Int = 20) {
    println("-".repeat(length))
}

fun functionsDemo() {
    val value = bmi(weightKg = 70.0, heightM = 1.75)   // іменовані аргументи
    println("ІМТ: ${"%.1f".format(value)}")
    println(greet("Олено"))
    println(greet("Андрію", greeting = "Доброго ранку"))
    printSeparator()
}
