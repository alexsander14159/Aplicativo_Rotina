package com.rotina.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Uma submeta é um item da rotina ligado a uma meta (ex.: "Horas de estudo" → "Estudar Java"). */
@Entity(
    tableName = "submetas",
    indices = [Index("metaId")],
)
data class Submeta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metaId: Long,
    val nome: String,
    val ordem: Int,
    /** Primeiro dia em que a submeta aparece (epoch day). 0 = desde sempre (submetas antigas). */
    @ColumnInfo(defaultValue = "0") val dataInicio: Long = 0L,
    /** Último dia em que a submeta aparece; null = sem fim. */
    @ColumnInfo(defaultValue = "NULL") val dataFim: Long? = null,
)
