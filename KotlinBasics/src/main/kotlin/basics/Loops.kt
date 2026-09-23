package basics

// Тема 2.4. Цикли
fun loopsDemo() {
    // for по діапазону: 1, 2, 3
    for (day in 1..3) {
        println("День $day")
    }

    // until — без останнього значення, step — крок
    for (hour in 0 until 24 step 6) {
        print("$hour:00 ")
    }
    println()

    // for по списку з індексами
    val weekSteps = listOf(5200, 8100, 3900)
    for ((index, value) in weekSteps.withIndex()) {
        println("Запис №${index + 1}: $value кроків")
    }

    // while — поки умова істинна
    var glasses = 0
    while (glasses < 3) {
        glasses++
    }
    println("Випито склянок води: $glasses")
}
