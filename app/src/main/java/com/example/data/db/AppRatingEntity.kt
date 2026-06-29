package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "app_ratings")
data class AppRatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerEmail: String,
    val ratingStars: Int, // 1 to 7 stars
    val comment: String,
    val ratingDate: Long
)

@Dao
interface AppRatingDao {
    @Query("SELECT * FROM app_ratings ORDER BY ratingDate DESC")
    fun getAllRatings(): Flow<List<AppRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: AppRatingEntity)

    @Query("DELETE FROM app_ratings")
    suspend fun clearRatings()
}
