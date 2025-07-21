package com.github.dictionary.model

import androidx.compose.runtime.saveable.Saver
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@Entity(tableName = "dict")
data class Dict(
    @PrimaryKey(autoGenerate = true) val _id: Int,
    val source: String?,
    val id: String?,
    val pid: String?,
    val name: String?,
    val innerId: String?,
    val time: String?,
    val downCount: String?,
    val exps: String?,
    val tiers: String?,
)


val DictSaver = Saver<Dict?, String>(
    save = {
        it ?: return@Saver null
        Json.encodeToString(it)
    },
    restore = {
        it ?: return@Saver null
        Json.decodeFromString<Dict>(it)
    }
)