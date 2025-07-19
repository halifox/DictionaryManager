package com.github.dictionary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dict")
data class Dict(
    @PrimaryKey(autoGenerate = true) val _id: Int,
    val source: String,
    val id: String,
    val pid: String,
    val name: String,
    val innerId: String,
    val time: String,
    val downCount: String,
    val exps: String,
    val tiers: Int,
)
