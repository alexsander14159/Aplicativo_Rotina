package com.rotina.ui

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTextTest {

    @Test
    fun parsearMarkdown_textoSimples_preservaConteudo() {
        val resultado = parsearMarkdown("Olá Jarvis!")
        assertEquals("Olá Jarvis!", resultado.text)
    }

    @Test
    fun parsearMarkdown_negrito_aplicaBoldSemAsteriscos() {
        val resultado = parsearMarkdown("Este é um **texto importante** para hoje.")
        assertEquals("Este é um texto importante para hoje.", resultado.text)
        
        // Deve haver pelo menos 1 SpanStyle com FontWeight.Bold
        val temBold = resultado.spanStyles.any { it.item.fontWeight == FontWeight.Bold }
        assertTrue("Deveria conter estilo negrito", temBold)
    }

    @Test
    fun parsearMarkdown_italico_aplicaItalicSemAsteriscos() {
        val resultado = parsearMarkdown("Lembre-se: *consistência* vence intensidade.")
        assertEquals("Lembre-se: consistência vence intensidade.", resultado.text)
        
        val temItalic = resultado.spanStyles.any { it.item.fontStyle == FontStyle.Italic }
        assertTrue("Deveria conter estilo itálico", temItalic)
    }

    @Test
    fun parsearMarkdown_itensDeLista_substituiPorBolinhas() {
        val markdown = """
            Metas do dia:
            * Estudo de Kotlin
            - 2 litros de água
            • Treino de perna
        """.trimIndent()

        val resultado = parsearMarkdown(markdown)
        assertTrue(resultado.text.contains("• Estudo de Kotlin"))
        assertTrue(resultado.text.contains("• 2 litros de água"))
        assertTrue(resultado.text.contains("• Treino de perna"))
    }
}
