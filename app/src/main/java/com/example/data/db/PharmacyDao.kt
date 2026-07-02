package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmacyDao {
    @Query("SELECT * FROM pharmacies ORDER BY createdAt DESC")
    fun getAllPharmaciesFlow(): Flow<List<PharmacyEntity>>

    @Query("SELECT * FROM pharmacies")
    suspend fun getAllPharmaciesSnapshot(): List<PharmacyEntity>

    @Query("SELECT * FROM pharmacies WHERE pharmacistEmail = :email LIMIT 1")
    suspend fun getPharmacyByPharmacistEmail(email: String): PharmacyEntity?

    @Query("SELECT * FROM pharmacies WHERE id = :id LIMIT 1")
    suspend fun getPharmacyById(id: Int): PharmacyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPharmacy(pharmacy: PharmacyEntity): Long

    @Query("UPDATE pharmacies SET isApproved = :isApproved WHERE id = :id")
    suspend fun updatePharmacyApproval(id: Int, isApproved: Boolean)

    @Query("DELETE FROM pharmacies WHERE id = :id")
    suspend fun deletePharmacy(id: Int)
}

@Dao
interface PharmacyProductDao {
    @Query("SELECT * FROM pharmacy_products WHERE pharmacyId = :pharmacyId ORDER BY createdAt DESC")
    fun getProductsByPharmacyFlow(pharmacyId: Int): Flow<List<PharmacyProductEntity>>

    @Query("SELECT * FROM pharmacy_products WHERE pharmacyId = :pharmacyId")
    suspend fun getProductsByPharmacySnapshot(pharmacyId: Int): List<PharmacyProductEntity>

    @Query("SELECT * FROM pharmacy_products ORDER BY createdAt DESC")
    fun getAllPharmacyProductsFlow(): Flow<List<PharmacyProductEntity>>

    @Query("SELECT * FROM pharmacy_products")
    suspend fun getAllPharmacyProductsSnapshot(): List<PharmacyProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: PharmacyProductEntity): Long

    @Query("UPDATE pharmacy_products SET isApproved = :isApproved WHERE id = :id")
    suspend fun updateProductApproval(id: Int, isApproved: Boolean)

    @Query("DELETE FROM pharmacy_products WHERE id = :id")
    suspend fun deleteProduct(id: Int)
}

@Dao
interface PharmacyOrderDao {
    @Query("SELECT * FROM pharmacy_orders ORDER BY createdAt DESC")
    fun getAllPharmacyOrdersFlow(): Flow<List<PharmacyOrderEntity>>

    @Query("SELECT * FROM pharmacy_orders")
    suspend fun getAllPharmacyOrdersSnapshot(): List<PharmacyOrderEntity>

    @Query("SELECT * FROM pharmacy_orders WHERE pharmacyId = :pharmacyId ORDER BY createdAt DESC")
    fun getOrdersByPharmacyFlow(pharmacyId: Int): Flow<List<PharmacyOrderEntity>>

    @Query("SELECT * FROM pharmacy_orders WHERE customerEmail = :email ORDER BY createdAt DESC")
    fun getOrdersByCustomerFlow(email: String): Flow<List<PharmacyOrderEntity>>

    @Query("SELECT * FROM pharmacy_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): PharmacyOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: PharmacyOrderEntity): Long

    @Query("UPDATE pharmacy_orders SET status = :status, medicinePrice = :medicinePrice, medicinesJson = :medicinesJson WHERE id = :id")
    suspend fun updateOrderPriceAndStatus(id: Int, status: String, medicinePrice: Double, medicinesJson: String)

    @Query("UPDATE pharmacy_orders SET status = :status, courierName = :courierName, courierPhone = :courierPhone, deliveryFee = :deliveryFee WHERE id = :id")
    suspend fun assignOrderCourierAndDeliveryFee(id: Int, status: String, courierName: String, courierPhone: String, deliveryFee: Double)

    @Query("UPDATE pharmacy_orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    @Query("UPDATE pharmacy_orders SET paymentMethod = :paymentMethod, bankReceiptImageUri = :receiptUri WHERE id = :id")
    suspend fun updatePharmacyOrderPayment(id: Int, paymentMethod: String, receiptUri: String?)

    @Query("DELETE FROM pharmacy_orders WHERE id = :id")
    suspend fun deleteOrder(id: Int)
}
