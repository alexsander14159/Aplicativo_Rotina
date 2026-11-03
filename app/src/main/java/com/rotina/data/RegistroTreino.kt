package com.rotina.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "registro_treino_dia")
data class RegistroTreinoDia(
    @PrimaryKey val data: Long,
    val concluido: Boolean = false,
    val alteracoesRotina: String = "",
    val fezCardio: Boolean = false,
    val tipoCardio: String = "",
    val cardioMinutos: Int = 0,
)

@Entity(
    tableName = "exercicios_registro",
    indices = [Index(value = ["data"])],
)
data class ExercicioRegistro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long,
    val nome: String,
    val series: Int,
    val repeticoes: String,
    val carga: String,
    val ordem: Int = 0,
)
