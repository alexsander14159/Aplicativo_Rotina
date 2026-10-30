package com.rotina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metas_dia")
data class Meta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val ordem: Int,
)
