package com.rotina.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metas_dia")
data class Meta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val ordem: Int,
    /** Alvo do dia em texto (ex.: "2h de estudo"). "" = sem alvo. defaultValue casa com a migration v1→v2. */
    @ColumnInfo(defaultValue = "") val alvoTexto: String = "",
)
