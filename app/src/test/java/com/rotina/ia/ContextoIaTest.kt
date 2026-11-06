package com.rotina.ia

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextoIaTest {

    @Test
    fun `resumoTexto mostra metas feitas e pendentes e todos os dados do dia`() {
        val contexto = contexto(
            metas = listOf("Horas de estudo (2h): Feita", "Água: Pendente"),
            submetas = listOf("Ler 10 páginas: Feita"),
            treinoPlanejado = "Peito e Triceps — Supino, crucifixo",
            treinoRealizado = true,
            exercicios = listOf("Supino Reto: 4 série(s) x 10 reps @ 30 kg"),
            cardio = "20 min Esteira",
            observacoes = "Treino curto por falta de tempo",
            diarioTexto = "meu dia foi produtivo",
        )

        val texto = resumoTexto(contexto)

        assertTrue(texto.contains("Feita"))
        assertTrue(texto.contains("Pendente"))
        assertTrue(texto.contains("Ler 10 páginas"))
        assertTrue(texto.contains("Peito e Triceps"))
        assertTrue(texto.contains("Treino realizado: Sim"))
        assertTrue(texto.contains("Supino Reto: 4 série(s) x 10 reps @ 30 kg"))
        assertTrue(texto.contains("20 min Esteira"))
        assertTrue(texto.contains("Treino curto por falta de tempo"))
        assertTrue(texto.contains("meu dia foi produtivo"))
    }

    @Test
    fun `resumoTexto de dia vazio informa nenhum registro e lista metas pendentes`() {
        val texto = resumoTexto(contexto(metas = listOf("Água: Pendente")))

        assertTrue(texto.startsWith("Nenhum registro no dia."))
        assertTrue(texto.contains("Água: Pendente"))
        assertFalse(texto.contains("Treino realizado: Sim"))
    }

    @Test
    fun `resumoTexto informa folga`() {
        val texto = resumoTexto(contexto(folga = true))

        assertTrue(texto.contains("Hoje é folga"))
    }

    @Test
    fun `resumoTexto inclui momentos rapidos com horario`() {
        val texto = resumoTexto(
            contexto(
                notasRapidas = listOf(
                    "09:30: 1ª refeição concluída, 500ml de água",
                    "14:15: Estudei Kotlin por 1 hora",
                ),
            ),
        )

        assertTrue(texto.contains("Momentos registrados ao longo do dia:"))
        assertTrue(texto.contains("09:30: 1ª refeição concluída, 500ml de água"))
        assertTrue(texto.contains("14:15: Estudei Kotlin por 1 hora"))
        assertFalse(texto.contains("Nenhum registro no dia."))
    }

    private fun contexto(
        folga: Boolean = false,
        metas: List<String> = emptyList(),
        submetas: List<String> = emptyList(),
        treinoPlanejado: String? = null,
        treinoRealizado: Boolean? = null,
        exercicios: List<String> = emptyList(),
        cardio: String? = null,
        observacoes: String? = null,
        diarioTexto: String = "",
        notasRapidas: List<String> = emptyList(),
    ) = ContextoDia(
        data = 20760,
        folga = folga,
        metas = metas,
        submetas = submetas,
        treinoPlanejado = treinoPlanejado,
        treinoRealizado = treinoRealizado,
        exercicios = exercicios,
        cardio = cardio,
        observacoes = observacoes,
        diarioTexto = diarioTexto,
        notasRapidas = notasRapidas,
        perfil = PerfilIa.PERFIL_PADRAO,
    )
}
