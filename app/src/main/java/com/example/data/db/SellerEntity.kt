package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sellers")
data class SellerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val classification: String = "تاجر ذهبي ⭐", // e.g. "ذهبي ⭐", "فضي 🥈", "برونزي 🥉"
    val commissionRate: Double = 0.10, // Default 10% commission for the platform
    val createdAt: Long = System.currentTimeMillis()
)
