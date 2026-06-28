package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val profileImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
