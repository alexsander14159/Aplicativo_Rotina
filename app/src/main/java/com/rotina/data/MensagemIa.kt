package com.rotina.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mensagens_ia",
    indices = [Index(value = ["data"]), Index(value = ["criadoEm"])],
)
data class MensagemIa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long, // epochDay do dia em que a conversa ocorreu
    val origem: String, // "USER" ou "ASSISTANT"
    val conteudo: String,
    val criadoEm: Long = System.currentTimeMillis(),
)
