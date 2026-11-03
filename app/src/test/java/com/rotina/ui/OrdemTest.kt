package com.rotina.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class OrdemTest {

    @Test
    fun trocarMovesItem() {
        val resultado = ordensAposTrocar(listOf(1, 2, 3, 4), de = 0, para = 2)
        assertEquals(listOf(2, 3, 1, 4), resultado)
    }

    @Test
    fun trocarTrocaUmaCasa() {
        val resultado = ordensAposTrocar(listOf(1, 2, 3), de = 1, para = 0)
        assertEquals(listOf(2, 1, 3), resultado)
    }

    @Test
    fun removerCompactaNoComeco() {
        val resultado = ordensAposRemover(listOf(1, 2, 3), pos = 0)
        assertEquals(listOf(1, 2), resultado)
    }

    @Test
    fun removerCompactaNoMeio() {
        val resultado = ordensAposRemover(listOf(1, 2, 3), pos = 1)
        assertEquals(listOf(1, 2), resultado)
    }
}
