package basics

// Тема 2.6. Класи
class UserProfile(val name: String, var age: Int) {
    var dailyStepGoal: Int = 8000   // властивість зі значенням за замовчуванням

    fun introduce(): String = "Я $name, мені $age р., моя ціль — $dailyStepGoal кроків"

    fun birthday() {
        age++
    }
}

// Тема 2.7. Data class — клас для зберігання даних
data class HealthRecord(
    val id: Int,
    val title: String,
    val value: Double,
    val unit: String
)

fun classesDemo() {
    val user = UserProfile(name = "Олена", age = 19)
    user.dailyStepGoal = 10_000
    user.birthday()
    println(user.introduce())

    val r1 = HealthRecord(1, "Пульс", 72.0, "уд/хв")
    val r2 = HealthRecord(1, "Пульс", 72.0, "уд/хв")
    println(r1)                              // toString() генерується автоматично
    println("r1 == r2: ${r1 == r2}")         // порівняння за вмістом
    val r3 = r1.copy(id = 2, value = 80.0)   // копія зі зміненими полями
    println(r3)
    val (_, title, value, unit) = r3         // деструктуризація
    println("$title = $value $unit")
}
