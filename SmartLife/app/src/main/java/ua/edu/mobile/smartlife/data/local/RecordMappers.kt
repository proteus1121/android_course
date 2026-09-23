package ua.edu.mobile.smartlife.data.local

import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType

// Перетворення між моделлю бази даних (RecordEntity) і моделлю застосунку (HealthRecord).
// Так решта коду не залежить від того, як саме дані лежать у таблиці.

fun RecordEntity.toModel() = HealthRecord(
    id = id,
    type = RecordType.valueOf(type),
    value = value,
    note = note,
    timestamp = timestamp,
    latitude = latitude,
    longitude = longitude
)

fun HealthRecord.toEntity() = RecordEntity(
    id = id,
    type = type.name,
    value = value,
    note = note,
    timestamp = timestamp,
    latitude = latitude,
    longitude = longitude
)
