package com.rotina.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val FORMATO_DIA_MES = DateTimeFormatter.ofPattern("dd/MM")
private val FORMATO_DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Resumo textual do dia (ex.: "4 de 6 concluídas (67%)"). */
fun resumoDoDia(concluidas: Int, total: Int): String {
    if (total <= 0) return "Nenhuma meta cadastrada"
    val percentual = (concluidas * 100.0 / total).roundToInt()
    return "$concluidas de $total concluídas ($percentual%)"
}

/** Rótulo da data: "Hoje, 03/11" quando é hoje; senão "02/11/2026". */
fun rotuloData(data: Long, hoje: Long): String {
    val dia = LocalDate.ofEpochDay(data)
    return if (data == hoje) {
        "Hoje, ${dia.format(FORMATO_DIA_MES)}"
    } else {
        dia.format(FORMATO_DIA_MES_ANO)
    }
}
