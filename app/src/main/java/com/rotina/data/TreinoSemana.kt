package com.rotina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treinos_semana")
data class TreinoSemana(
    /** 1 = Segunda, 2 = Terça, ..., 7 = Domingo (DayOfWeek.value) */
    @PrimaryKey val diaSemana: Int,
    val titulo: String,
    val descricao: String = "",
)
