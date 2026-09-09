package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_cities")
data class SavedCityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val state: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
