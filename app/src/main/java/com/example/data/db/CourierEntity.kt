package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "couriers")
data class CourierEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val stateInfo: String,
    val status: String // e.g. "نشط ومتوفر 🟢", "في مهمة توصيل 🟡", "غير متوفر 🔴"
)
