package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders", primaryKeys = ["orderId", "productId"])
data class OrderEntity(
    val orderId: String,
    val productId: Int,
    val productName: String,
    val priceAtOrder: Double,
    val quantity: Int,
    val orderDate: Long,
    val statusArabic: String, // e.g. "في انتظار الشحن" or "تم التوصيل"
    val customerName: String = "غير معروف",
    val customerPhone: String = "غير معروف",
    val customerAddress: String = "السودان",
    val courierName: String = "",
    val courierPhone: String = "",
    val deliveryFee: Double = 0.0,
    val paymentMethod: String = "كاش",
    val bankReceiptImageUri: String? = null
)
