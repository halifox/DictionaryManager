package com.github.dictionary.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "record")
data class LocalRecord(
    @PrimaryKey(autoGenerate = false) val _id: Int,
    val ids: List<Long>
)

class Converters {
    @TypeConverter
    fun fromIds(ids: List<Long>): String = ids.joinToString(",")

    @TypeConverter
    fun toIds(data: String): List<Long> =
        if (data.isEmpty()) emptyList() else data.split(",").map { it.toLong() }
}