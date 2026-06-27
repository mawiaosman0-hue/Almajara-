package com.example.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "admin_managers")
data class AdminManagerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface AdminManagerDao {
    @Query("SELECT * FROM admin_managers ORDER BY id DESC")
    fun getAllAdminManagers(): Flow<List<AdminManagerEntity>>

    @Query("SELECT * FROM admin_managers")
    suspend fun getAllAdminManagersSnapshot(): List<AdminManagerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminManager(admin: AdminManagerEntity): Long

    @Query("DELETE FROM admin_managers WHERE id = :id")
    suspend fun deleteAdminManager(id: Int)
}
