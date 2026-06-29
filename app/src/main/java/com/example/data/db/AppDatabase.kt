package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class, 
        CartEntity::class, 
        OrderEntity::class, 
        ProfileEntity::class, 
        CourierEntity::class, 
        SellerEntity::class,
        PharmacyEntity::class,
        PharmacyProductEntity::class,
        PharmacyOrderEntity::class,
        AdminManagerEntity::class,
        RestaurantEntity::class,
        RestaurantOrderEntity::class,
        AppRatingEntity::class,
        AppCouponEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun profileDao(): ProfileDao
    abstract fun courierDao(): CourierDao
    abstract fun sellerDao(): SellerDao
    abstract fun pharmacyDao(): PharmacyDao
    abstract fun pharmacyProductDao(): PharmacyProductDao
    abstract fun pharmacyOrderDao(): PharmacyOrderDao
    abstract fun adminManagerDao(): AdminManagerDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun restaurantOrderDao(): RestaurantOrderDao
    abstract fun appRatingDao(): AppRatingDao
    abstract fun appCouponDao(): AppCouponDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "majarah_ecommerce_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
