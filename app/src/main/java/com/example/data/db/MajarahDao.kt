package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: Int)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :productId")
    suspend fun updateFavorite(productId: Int, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductsCount(): Int

    @Query("UPDATE products SET stock = MAX(0, stock - :quantity) WHERE id = :productId")
    suspend fun decrementStock(productId: Int, quantity: Int)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItemByProduct(productId: Int)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateCartQuantity(productId: Int, quantity: Int)

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProduct(productId: Int): CartEntity?

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("UPDATE orders SET statusArabic = :newStatus WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String)

    @Query("UPDATE orders SET statusArabic = :newStatus, courierName = :courierName, courierPhone = :courierPhone WHERE orderId = :orderId")
    suspend fun updateOrderStatusAndCourier(orderId: String, newStatus: String, courierName: String, courierPhone: String)

    @Query("UPDATE orders SET statusArabic = :newStatus, courierName = :courierName, courierPhone = :courierPhone, deliveryFee = :deliveryFee WHERE orderId = :orderId")
    suspend fun updateOrderStatusAndCourierWithFee(orderId: String, newStatus: String, courierName: String, courierPhone: String, deliveryFee: Double)

    @Query("DELETE FROM orders WHERE orderId NOT IN (:remoteOrderIds)")
    suspend fun deleteOrdersNotIn(remoteOrderIds: List<String>)

    @Query("DELETE FROM orders")
    suspend fun clearOrderHistory()

    @androidx.room.Transaction
    suspend fun syncOrdersTransaction(orders: List<OrderEntity>) {
        if (orders.isNotEmpty()) {
            val remoteOrderIds = orders.map { it.orderId }.distinct()
            deleteOrdersNotIn(remoteOrderIds)
            insertOrders(orders)
        } else {
            clearOrderHistory()
        }
    }

    @Query("SELECT * FROM orders")
    suspend fun getAllOrdersSnapshot(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): List<OrderEntity>
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id DESC")
    suspend fun getAllProfiles(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun clearProfiles()
}

@Dao
interface CourierDao {
    @Query("SELECT * FROM couriers ORDER BY id DESC")
    fun getAllCouriers(): kotlinx.coroutines.flow.Flow<List<CourierEntity>>

    @Query("SELECT * FROM couriers")
    suspend fun getAllCouriersSnapshot(): List<CourierEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourier(courier: CourierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCouriers(couriers: List<CourierEntity>)

    @Query("DELETE FROM couriers WHERE id = :id")
    suspend fun deleteCourier(id: Int)

    @Query("SELECT COUNT(*) FROM couriers")
    suspend fun getCouriersCount(): Int
}

@Dao
interface SellerDao {
    @Query("SELECT * FROM sellers ORDER BY id DESC")
    fun getAllSellers(): kotlinx.coroutines.flow.Flow<List<SellerEntity>>

    @Query("SELECT * FROM sellers")
    suspend fun getAllSellersSnapshot(): List<SellerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: SellerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSellers(sellers: List<SellerEntity>)

    @Query("DELETE FROM sellers WHERE id = :id")
    suspend fun deleteSeller(id: Int)

    @Query("SELECT COUNT(*) FROM sellers")
    suspend fun getSellersCount(): Int
}

