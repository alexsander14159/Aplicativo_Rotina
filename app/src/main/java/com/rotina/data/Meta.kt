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
    /** Primeiro dia em que a meta aparece (epoch day). 0 = desde sempre (metas antigas). */
    @ColumnInfo(defaultValue = "0") val dataInicio: Long = 0L,
    /** Último dia em que a meta aparece; null = sem fim. */
    @ColumnInfo(defaultValue = "NULL") val dataFim: Long? = null,
)
