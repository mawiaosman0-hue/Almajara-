package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double, // in Sudanese Pound (SDG)
    val category: String, // e.g. "electronics", "fashion", "home", "cosmic_deals"
    val categoryArabic: String,
    val rating: Float,
    val imageResName: String, // code to display elegant custom icons dynamically
    val isFavorite: Boolean = false,
    val stock: Int = 10,
    val sellerEmail: String = "",
    val isApproved: Boolean = true
)
