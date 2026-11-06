package com.rotina.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notas_dia",
    indices = [Index(value = ["data"]), Index(value = ["criadoEm"])],
)
data class NotaDia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long, // epochDay
    val hora: String, // ex: "09:30"
    val texto: String,
    val criadoEm: Long = System.currentTimeMillis(),
)
