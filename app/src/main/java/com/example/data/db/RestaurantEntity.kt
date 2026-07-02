package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val menuImageUri: String? = null,
    val logoImageUri: String? = null,
    val isApproved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "restaurant_orders")
data class RestaurantOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantId: Int,
    val restaurantName: String,
    val restaurantPhone: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val itemsAndNotes: String,
    val status: String = "معلق", // معلق, قيد التحضير, تم التسليم
    val paymentMethod: String = "كاش", // كاش, تحويل بنكي
    val deliveryFee: Double = 0.0,
    val bankReceiptImageUri: String? = null,
    val courierName: String = "",
    val courierPhone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
