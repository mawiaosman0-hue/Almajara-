package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pharmacies")
data class PharmacyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val doctorName: String,
    val phone: String, // WhatsApp phone
    val location: String,
    val pharmacistEmail: String, // linked pharmacist user
    val isApproved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
