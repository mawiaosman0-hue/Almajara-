package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants ORDER BY createdAt DESC")
    fun getAllRestaurantsFlow(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants")
    suspend fun getAllRestaurantsSnapshot(): List<RestaurantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity): Long

    @Query("DELETE FROM restaurants WHERE id = :id")
    suspend fun deleteRestaurant(id: Int)

    @Query("UPDATE restaurants SET isApproved = :approved WHERE id = :id")
    suspend fun updateRestaurantApproval(id: Int, approved: Boolean)
}

@Dao
interface RestaurantOrderDao {
    @Query("SELECT * FROM restaurant_orders ORDER BY createdAt DESC")
    fun getAllRestaurantOrdersFlow(): Flow<List<RestaurantOrderEntity>>

    @Query("SELECT * FROM restaurant_orders")
    suspend fun getAllRestaurantOrdersSnapshot(): List<RestaurantOrderEntity>

    @Query("SELECT * FROM restaurant_orders WHERE customerEmail = :email ORDER BY createdAt DESC")
    fun getOrdersByCustomerFlow(email: String): Flow<List<RestaurantOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: RestaurantOrderEntity): Long

    @Query("UPDATE restaurant_orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    @Query("UPDATE restaurant_orders SET paymentMethod = :paymentMethod, bankReceiptImageUri = :receiptUri WHERE id = :id")
    suspend fun updateRestaurantOrderPayment(id: Int, paymentMethod: String, receiptUri: String?)

    @Query("UPDATE restaurant_orders SET status = :status, courierName = :courierName, courierPhone = :courierPhone, deliveryFee = :deliveryFee WHERE id = :id")
    suspend fun assignCourierToRestaurantOrder(id: Int, status: String, courierName: String, courierPhone: String, deliveryFee: Double)

    @Query("DELETE FROM restaurant_orders WHERE id = :id")
    suspend fun deleteOrder(id: Int)
}
