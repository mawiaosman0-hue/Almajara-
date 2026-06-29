package com.example.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "app_coupons")
data class AppCouponEntity(
    @PrimaryKey val code: String,
    val discountPercent: Double = 0.0,
    val isFreeDelivery: Boolean = false,
    val isBogo: Boolean = false, // buy one get one free
    val forUserEmail: String,
    val isUsed: Boolean = false,
    val offerTitle: String = ""
)

@Dao
interface AppCouponDao {
    @Query("SELECT * FROM app_coupons")
    fun getAllCoupons(): Flow<List<AppCouponEntity>>

    @Query("SELECT * FROM app_coupons WHERE code = :code LIMIT 1")
    suspend fun getCouponByCode(code: String): AppCouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: AppCouponEntity)

    @Query("UPDATE app_coupons SET isUsed = 1 WHERE code = :code")
    suspend fun markCouponAsUsed(code: String)

    @Query("DELETE FROM app_coupons")
    suspend fun clearCoupons()
}
