package basics

fun main() {
    val demos = listOf(
        "2.1–2.2 Змінні та типи" to ::variablesDemo,
        "2.3 Умови" to ::conditionsDemo,
        "2.4 Цикли" to ::loopsDemo,
        "2.5 Функції" to ::functionsDemo,
        "2.6–2.7 Класи та data class" to ::classesDemo,
        "2.8 Null safety" to ::nullSafetyDemo,
        "2.9–2.10 Колекції та лямбди" to ::collectionsDemo,
        "2.11 Об'єкти" to ::objectsDemo
    )
    for ((title, demo) in demos) {
        println("===== $title =====")
        demo()
        println()
    }
}
