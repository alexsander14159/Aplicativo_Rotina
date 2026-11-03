package com.rotina.ui

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumoTest {

    @Test
    fun `resumo arredonda o percentual`() {
        assertEquals("4 de 6 concluídas (67%)", resumoDoDia(concluidas = 4, total = 6))
    }

    @Test
    fun `resumo com todas concluidas`() {
        assertEquals("6 de 6 concluídas (100%)", resumoDoDia(concluidas = 6, total = 6))
    }

    @Test
    fun `resumo sem metas cadastradas`() {
        assertEquals("Nenhuma meta cadastrada", resumoDoDia(concluidas = 0, total = 0))
    }

    @Test
    fun `rotulo de hoje mostra Hoje e dia mes`() {
        val hoje = LocalDate.of(2026, 11, 3).toEpochDay()
        assertEquals("Hoje, 03/11", rotuloData(data = hoje, hoje = hoje))
    }

    @Test
    fun `rotulo de outro dia mostra data completa`() {
        val hoje = LocalDate.of(2026, 11, 3).toEpochDay()
        val anterior = LocalDate.of(2026, 11, 2).toEpochDay()
        assertEquals("02/11/2026", rotuloData(data = anterior, hoje = hoje))
    }
}
