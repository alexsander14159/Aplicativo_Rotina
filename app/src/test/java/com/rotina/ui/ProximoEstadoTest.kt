package com.rotina.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProximoEstadoTest {
    @Test
    fun sem_registro_e_desmarcada_passam_a_marcada() {
        assertTrue(proximoEstado(null))
        assertTrue(proximoEstado(false))
    }

    @Test
    fun marcada_passa_a_desmarcada() {
        assertFalse(proximoEstado(true))
    }
}
