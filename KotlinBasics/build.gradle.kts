plugins {
    kotlin("jvm") version "2.4.20"
    application
}

application {
    // Файл Main.kt з пакетом basics -> клас basics.MainKt
    mainClass.set("basics.MainKt")
    // Щоб українські літери коректно виводилися в консоль Windows
    applicationDefaultJvmArgs = listOf("-Dstdout.encoding=UTF-8")
}
