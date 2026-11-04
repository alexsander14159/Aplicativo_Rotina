package com.rotina.ui

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VigenciaTest {

    private val hoje = LocalDate.of(2026, 11, 3).toEpochDay()
    private val futuro = LocalDate.of(2026, 11, 5).toEpochDay()

    @Test
    fun `meta futura nao aparece hoje`() {
        assertFalse(vigenteNaData(dataInicio = futuro, dataFim = null, data = hoje))
    }

    @Test
    fun `meta aparece no dia de inicio e depois`() {
        assertTrue(vigenteNaData(dataInicio = hoje, dataFim = null, data = hoje))
        assertTrue(vigenteNaData(dataInicio = hoje, dataFim = null, data = hoje + 10))
    }

    @Test
    fun `meta aparece no ultimo dia e some depois`() {
        val fim = hoje + 2
        assertTrue(vigenteNaData(dataInicio = hoje, dataFim = fim, data = fim))
        assertFalse(vigenteNaData(dataInicio = hoje, dataFim = fim, data = fim + 1))
    }

    @Test
    fun `exclusao definitiva somente na data de inicio`() {
        assertTrue(podeApagarDefinitivamente(dataInicio = hoje, dataSelecionada = hoje))
        assertFalse(podeApagarDefinitivamente(dataInicio = hoje - 1, dataSelecionada = hoje))
    }
}
