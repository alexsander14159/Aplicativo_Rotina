package com.rotina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Diário de um dia; a data é o epoch day do aparelho. */
@Entity(tableName = "diario_dia")
data class DiarioDia(
    @PrimaryKey val data: Long,
    val texto: String = "",
    val atualizadoEm: Long = System.currentTimeMillis(),
)
