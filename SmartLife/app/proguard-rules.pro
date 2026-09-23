# Правила R8/ProGuard для release-збірки Smart Life.
# Більшість бібліотек (Retrofit, OkHttp, kotlinx.serialization, Room, Coil) містять
# власні правила всередині AAR, тому тут — лише доповнення.

# Класи DTO серіалізуються за іменами полів — зберігаємо їх (з анотацією @Serializable)
-keepclassmembers @kotlinx.serialization.Serializable class ua.edu.mobile.smartlife.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# osmdroid звертається до деяких класів через рефлексію
-dontwarn org.osmdroid.**

# Номери рядків у стек-трейсах (зручно для аналізу збоїв), але без оригінальних імен файлів
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
