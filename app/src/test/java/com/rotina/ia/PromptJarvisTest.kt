package com.rotina.ia

import com.rotina.data.MensagemIa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptJarvisTest {

    @Test
    fun `historicoParaEnvio mantem as 30 mais recentes em ordem ascendente`() {
        val mensagens = (0 until 35).map { i ->
            MensagemIa(
                id = i + 1L,
                data = 20760,
                origem = if (i % 2 == 0) "USER" else "ASSISTANT",
                conteudo = "mensagem $i",
                criadoEm = i.toLong(),
            )
        }.shuffled()

        val resultado = historicoParaEnvio(mensagens)

        assertEquals(30, resultado.size)
        assertEquals(listOf(6L, 7L), listOf(resultado.first().id, resultado[1].id))
        assertEquals(35L, resultado.last().id)
        assertTrue(resultado.zipWithNext().all { (a, b) -> a.criadoEm <= b.criadoEm })
    }

    @Test
    fun `historicoParaEnvio devolve tudo quando ha menos de 30 em ordem ascendente`() {
        val mensagens = listOf(
            MensagemIa(id = 3L, data = 20760, origem = "ASSISTANT", conteudo = "c", criadoEm = 30L),
            MensagemIa(id = 1L, data = 20760, origem = "USER", conteudo = "a", criadoEm = 10L),
            MensagemIa(id = 2L, data = 20760, origem = "ASSISTANT", conteudo = "b", criadoEm = 20L),
        )

        val resultado = historicoParaEnvio(mensagens)

        assertEquals(listOf(1L, 2L, 3L), resultado.map { it.id })
    }

    @Test
    fun `historicoParaEnvio devolve vazio para lista vazia`() {
        assertTrue(historicoParaEnvio(emptyList()).isEmpty())
    }

    @Test
    fun `montar usa system com perfil e resumo e mapeia papeis`() {
        val contexto = contexto(
            metas = listOf("Água: Feita"),
            perfil = "Perfil customizado: gosta de treinar cedo.",
        )
        val historico = listOf(
            MensagemIa(id = 1L, data = 20760, origem = "USER", conteudo = "Oi, Jarvis", criadoEm = 10L),
            MensagemIa(id = 2L, data = 20760, origem = "ASSISTANT", conteudo = "Olá!", criadoEm = 20L),
        )

        val prompts = montar(contexto, historico, "O que devo melhorar?")

        assertEquals("system", prompts[0].papel)
        assertTrue(prompts[0].texto.contains(PerfilIa.PERFIL_PADRAO))
        assertTrue(prompts[0].texto.contains("Perfil customizado"))
        assertTrue(prompts[0].texto.contains("Água: Feita"))
        assertEquals(listOf("user", "model", "user"), prompts.drop(1).map { it.papel })
        assertEquals("Oi, Jarvis", prompts[1].texto)
        assertEquals("Olá!", prompts[2].texto)
        assertEquals("O que devo melhorar?", prompts[3].texto)
    }

    @Test
    fun `montar sem pergunta cria pedido de briefing`() {
        val prompts = montar(contexto(), emptyList(), null)

        assertEquals(2, prompts.size)
        assertEquals("system", prompts[0].papel)
        assertEquals("user", prompts[1].papel)
        assertTrue(prompts[1].texto.contains("briefing", ignoreCase = true))
    }

    private fun contexto(
        metas: List<String> = emptyList(),
        perfil: String = PerfilIa.PERFIL_PADRAO,
    ) = ContextoDia(
        data = 20760,
        folga = false,
        metas = metas,
        submetas = emptyList(),
        treinoPlanejado = null,
        treinoRealizado = null,
        exercicios = emptyList(),
        cardio = null,
        observacoes = null,
        diarioTexto = "",
        perfil = perfil,
    )
}
