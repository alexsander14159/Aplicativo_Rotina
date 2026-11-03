package com.rotina.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistroResumoTest {

    @Test
    fun `exercicio completo formata series reps e carga`() {
        assertEquals(
            "Supino Reto — 4 séries • 10 reps • 30 kg",
            resumoExercicio("Supino Reto", 4, "10", "30 kg"),
        )
    }

    @Test
    fun `exercicio com uma serie usa singular`() {
        assertEquals(
            "Rosca — 1 série • 12 reps • 10 kg",
            resumoExercicio("Rosca", 1, "12", "10 kg"),
        )
    }

    @Test
    fun `exercicio omite reps e carga vazios`() {
        assertEquals(
            "Agachamento — 3 séries",
            resumoExercicio("Agachamento", 3, " ", ""),
        )
    }

    @Test
    fun `exercicio sem detalhes mostra so o nome`() {
        assertEquals("Prancha", resumoExercicio("Prancha", 0, "", ""))
    }

    @Test
    fun `cardio completo`() {
        assertEquals("Cardio: 20 min (Esteira)", resumoCardio(true, "Esteira", 20))
    }

    @Test
    fun `cardio sem tipo mostra so duracao`() {
        assertEquals("Cardio: 20 min", resumoCardio(true, "", 20))
    }

    @Test
    fun `cardio ausente retorna null`() {
        assertNull(resumoCardio(false, "Esteira", 20))
    }

    @Test
    fun `quantidade de exercicios`() {
        assertNull(resumoExerciciosAnotados(0))
        assertEquals("Exercícios: 1 anotado", resumoExerciciosAnotados(1))
        assertEquals("Exercícios: 3 anotados", resumoExerciciosAnotados(3))
    }
}
