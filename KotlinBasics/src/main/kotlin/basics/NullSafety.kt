package basics

// Тема 2.8. Null safety
fun nullSafetyDemo() {
    var nickname: String? = null   // знак ? — змінна може містити null
    // println(nickname.length)    // помилка компіляції: nickname може бути null

    println("Довжина: ${nickname?.length}")              // ?. — безпечний виклик -> null
    println("Ім'я: ${nickname ?: "Гість"}")              // ?: — оператор Елвіса

    nickname = "runner_ua"
    if (nickname != null) {
        // Smart cast: усередині if компілятор знає, що це String
        println("Довжина після перевірки: ${nickname.length}")
    }

    // let виконується, лише якщо значення не null
    val email: String? = "student@univ.edu.ua"
    email?.let { println("Надсилаємо лист на $it") }

    // Небезпечно: !! кидає NullPointerException, якщо значення null
    val city: String? = "Київ"
    println("Місто: ${city!!.uppercase()}")
}
