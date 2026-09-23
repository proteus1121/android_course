package basics

// Тема 2.9. Колекції та 2.10. Лямбда-вирази
fun collectionsDemo() {
    val readOnly = listOf("Пульс", "Вода", "Сон")          // незмінний список
    val tags = mutableListOf("ранок")                        // змінний список
    tags.add("тренування")
    tags.remove("ранок")
    println("Списки: $readOnly, $tags, перший: ${readOnly.first()}")

    val units = mapOf("Пульс" to "уд/хв", "Вода" to "л")   // словник (ключ -> значення)
    println("Одиниця для води: ${units["Вода"]}")

    val uniqueDays = setOf("пн", "вт", "пн")                // множина без повторів
    println("Унікальні дні: $uniqueDays")

    val records = listOf(
        HealthRecord(1, "Вода", 0.5, "л"),
        HealthRecord(2, "Пульс", 72.0, "уд/хв"),
        HealthRecord(3, "Вода", 0.3, "л"),
        HealthRecord(4, "Пульс", 95.0, "уд/хв")
    )

    // Лямбда — функція без імені: { параметр -> тіло }
    val water = records.filter { it.title == "Вода" }
    val totalWater = water.sumOf { it.value }
    val titles = records.map { it.title }.distinct()
    val maxPulse = records.filter { it.title == "Пульс" }.maxOf { it.value }
    val byTitle = records.groupBy { it.title }

    println("Усього води: $totalWater л")
    println("Типи записів: $titles")
    println("Максимальний пульс: $maxPulse")
    println("Кількість за типом: ${byTitle.mapValues { it.value.size }}")

    // Лямбду можна зберегти у змінну та передати у функцію
    val isHigh: (HealthRecord) -> Boolean = { it.title == "Пульс" && it.value > 90 }
    println("Високий пульс: ${records.any(isHigh)}")
    records.forEach { println(" • ${it.title}: ${it.value} ${it.unit}") }
}
