package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pharmacy_orders")
data class PharmacyOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pharmacyId: Int,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String = "",
    val prescriptionImageBase64: String = "", // Base64 encoding for captured prescription photo
    val medicinesJson: String = "", // List of medicines with prices, e.g., JSON or comma-separated
    val medicinePrice: Double = 0.0, // Flipped or calculated price of medicines by pharmacist
    val deliveryFee: Double = 0.0, // determined by Admin
    val courierName: String = "", // Courier assigned by Admin
    val courierPhone: String = "",
    val status: String = "بانتظار الصيدلي", // "بانتظار الصيدلي", "بانتظار المدير", "تم تحديد السعر النهائي", "جاري التجهيز للتوصيل", "تم التوصيل"
    val paymentMethod: String = "كاش",
    val bankReceiptImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
