package com.rotina.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Marcação diária de uma submeta: única por (data, submetaId). */
@Entity(
    tableName = "submeta_dia",
    indices = [Index(value = ["data", "submetaId"], unique = true)],
)
data class SubmetaDia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** LocalDate.toEpochDay() do aparelho. */
    val data: Long,
    val submetaId: Long,
    val concluida: Boolean,
)
