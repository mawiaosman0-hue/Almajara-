package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pharmacy_products")
data class PharmacyProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pharmacyId: Int,
    val type: String, // "دواء" (Medicine) or "كوزمتك" (Cosmetics)
    val name: String,
    val company: String,
    val price: Double,
    val imageBase64: String = "", // Base64 encoding for captured photo or selected gallery image
    val isApproved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
