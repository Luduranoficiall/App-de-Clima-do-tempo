package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCityDao {
    @Query("SELECT * FROM saved_cities ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllSavedCities(): Flow<List<SavedCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: SavedCityEntity): Long

    @Delete
    suspend fun deleteCity(city: SavedCityEntity)

    @Query("DELETE FROM saved_cities WHERE id = :id")
    suspend fun deleteCityById(id: Long)

    @Query("SELECT * FROM saved_cities WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): SavedCityEntity?

    @Query("SELECT COUNT(*) FROM saved_cities")
    suspend fun getCount(): Int
}
