package com.rotina.audio

import android.speech.SpeechRecognizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultadoVozTest {

    @Test
    fun `parcial usa o primeiro resultado nao vazio`() {
        assertEquals(
            "hoje",
            textoParcial(listOf("hoje", "", "estou estudando")),
        )
    }

    @Test
    fun `parcial vazio retorna null`() {
        assertNull(textoParcial(emptyList()))
        assertNull(textoParcial(listOf("", "   ")))
    }

    @Test
    fun `final usa o primeiro resultado nao vazio`() {
        assertEquals(
            "Hoje estudei Java",
            textoFinal(listOf("Hoje estudei Java", "outra opcao")),
        )
    }

    @Test
    fun `final vazio retorna null`() {
        assertNull(textoFinal(emptyList()))
        assertNull(textoFinal(listOf("", "   ")))
    }

    @Test
    fun `erro sem correspondencia tem mensagem amigavel`() {
        assertEquals(
            "Não entendi o áudio. Tente falar mais perto do microfone.",
            mensagemErroReconhecimento(SpeechRecognizer.ERROR_NO_MATCH),
        )
    }

    @Test
    fun `erro de muitas requisicoes tem mensagem amigavel`() {
        assertEquals(
            "Muitas tentativas de voz. Aguarde um pouco e tente novamente.",
            mensagemErroReconhecimento(SpeechRecognizer.ERROR_TOO_MANY_REQUESTS),
        )
    }

    @Test
    fun `erro de idioma indisponivel tem mensagem amigavel`() {
        assertEquals(
            "O idioma português não está disponível no reconhecimento de voz.",
            mensagemErroReconhecimento(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE),
        )
    }

    @Test
    fun `erro desconhecido inclui o codigo`() {
        assertEquals(
            "Não foi possível reconhecer a fala (código 999).",
            mensagemErroReconhecimento(999),
        )
    }
}
