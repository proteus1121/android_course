# Програмування мобільних платформ

Матеріали до навчального посібника «Програмування мобільних платформ. Від першого застосунку до повноцінного проєкту на Kotlin та Jetpack Compose» (Іщенко А. Д., Русаловський В. Б., ДУІТЗ, 2026).

## Вміст репозиторію

| Папка | Що всередині |
|---|---|
| `posibnyk/` | посібник (PDF і DOCX) та вихідні коди схем PlantUML (`diagrams/*.puml`) |
| `HelloMobile/` | перший застосунок на Jetpack Compose (підрозділ 1.3) |
| `KotlinBasics/` | приклади основ мови Kotlin (підрозділ 1.4) |
| `SmartLife/` | наскрізний проєкт курсу – застосунок моніторингу здоров'я Smart Life |

## Вимоги

- Android Studio 2025.3.3 Patch 1 (Panda 3) або новіша;
- JDK 17+ (достатньо вбудованого в Android Studio);
- емулятор або телефон з Android 8.0+ (`minSdk 26`), рекомендовано Android 15–17.

## Запуск Smart Life

1. Клонуйте репозиторій: `git clone https://github.com/proteus1121/android_course.git`.
2. В Android Studio відкрийте папку `SmartLife` (*File ▸ Open*) і дочекайтеся Gradle Sync.
3. Створіть емулятор у *Device Manager* і натисніть *Run*.
4. Увійдіть як `emilys` / `emilyspass` (демонстраційний обліковий запис DummyJSON).

Збірка з командного рядка (з папки `SmartLife`): `./gradlew assembleDebug`, тести – `./gradlew testDebugUnitTest`.

Файли `local.properties`, `keystore.properties` і ключ підпису `*.jks` у репозиторій не входять: кожен створює їх самостійно (див. підрозділ 5.5 і додаток В посібника).

## Етапи проєкту

Стан Smart Life після кожної теми збережено міткою git. Щоб переглянути проєкт на потрібному етапі, виконайте `git checkout <мітка>`, а щоб повернутися до останньої версії – `git checkout main`. Зміни між етапами показує `git diff stage-06 stage-07`.

| Мітка | Тема | Підрозділ посібника |
|---|---|---|
| `stage-03` | прототип інтерфейсу, навігація | 2.2 |
| `stage-04` | стан і ViewModel | 2.3 |
| `stage-05` | архітектура MVVM, репозиторій, AppContainer | 2.4 |
| `stage-06` | Room, DataStore, налаштування | 3.3 |
| `stage-07` | мережа: Retrofit, Open-Meteo | 3.4 |
| `stage-08` | Bluetooth LE | 4.3 |
| `stage-09` | геолокація | 4.4 |
| `stage-10` | карта osmdroid | 4.4 |
| `stage-11` | камера і галерея | 4.5 |
| `stage-12` | сенсори | 4.6 |
| `stage-13` | сповіщення | 5.2 |
| `stage-14` | фонові задачі (WorkManager) | 5.3 |
| `stage-15` | авторизація | 5.4 |
| `stage-16` | безпека, підпис релізу | 5.5 |
| `stage-17` | тестування | 6.2 |
| `stage-18`, `v1.0` | фінальна версія Smart Life 1.0 | 6.3 |

## Схеми

Усі схеми посібника створено в PlantUML. Щоб переглянути або змінити схему, відкрийте файл із `posibnyk/diagrams` на https://www.planttext.com/ або в плагіні PlantUML для IDE.
