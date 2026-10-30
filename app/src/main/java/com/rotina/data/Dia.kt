package com.rotina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Anotação de um dia: travar o dia como folga (feriado etc.). Chave natural = data. */
@Entity(tableName = "dias")
data class Dia(
    @PrimaryKey val data: Long,
    val folga: Boolean,
)
