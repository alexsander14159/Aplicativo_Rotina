package com.rotina.ia

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiClientTest {

    @Test
    fun `chave vazia retorna erro sem tentar a rede`() = runBlocking {
        val resultado = GeminiClient.enviar("   ", emptyList())

        assertTrue(resultado.isFailure)
        assertEquals(
            "Chave de API do Gemini não configurada. Toque no ícone de chave/engrenagem para configurar.",
            resultado.exceptionOrNull()?.message,
        )
    }
}
