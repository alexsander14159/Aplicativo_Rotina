package com.rotina.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** Estados da sessão de voz; "processando" ainda espera onResults/onError. */
enum class EstadoGravacao { OCIOSO, OUVINDO, PROCESSANDO }

/**
 * Encapsula o SpeechRecognizer nativo. Os callbacks são entregues na thread principal
 * e atualizam o estado da tela; nenhum áudio é gravado em arquivo.
 */
class GravadorVoz(
    context: Context,
    private val aoParcial: (String) -> Unit,
    private val aoFinal: (String) -> Unit,
    private val aoErro: (String) -> Unit,
    private val aoEstado: (EstadoGravacao) -> Unit,
) : RecognitionListener {

    private var estado = EstadoGravacao.OCIOSO
    private var paradaManual = false
    private var ignorarCallbacks = false

    private val reconhecedor: SpeechRecognizer? = runCatching {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            null
        } else {
            SpeechRecognizer.createSpeechRecognizer(context.applicationContext).also {
                it.setRecognitionListener(this)
            }
        }
    }.getOrNull()

    val disponivel: Boolean get() = reconhecedor != null

    fun iniciar() {
        if (estado != EstadoGravacao.OCIOSO) return
        val r = reconhecedor
        if (r == null) {
            aoErro("Reconhecimento de voz indisponível neste aparelho. Digite o diário.")
            return
        }
        ignorarCallbacks = false
        paradaManual = false
        estado = EstadoGravacao.OUVINDO
        aoEstado(EstadoGravacao.OUVINDO)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            r.startListening(intent)
        } catch (e: Exception) {
            estado = EstadoGravacao.OCIOSO
            aoEstado(EstadoGravacao.OCIOSO)
            aoErro("Não foi possível iniciar o microfone: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    fun parar() {
        if (estado != EstadoGravacao.OUVINDO) return
        paradaManual = true
        estado = EstadoGravacao.PROCESSANDO
        aoEstado(EstadoGravacao.PROCESSANDO)

        val parou = runCatching { reconhecedor?.stopListening() }.isSuccess
        if (!parou) {
            estado = EstadoGravacao.OCIOSO
            aoEstado(EstadoGravacao.OCIOSO)
            aoErro("Não foi possível parar o reconhecimento de voz. Tente novamente.")
        }
    }

    /** Interrompe sem salvar resultado (app foi para segundo plano ou tela saiu). */
    fun cancelar() {
        ignorarCallbacks = true
        paradaManual = true
        runCatching { reconhecedor?.cancel() }
        estado = EstadoGravacao.OCIOSO
        aoEstado(EstadoGravacao.OCIOSO)
    }

    fun destruir() {
        ignorarCallbacks = true
        estado = EstadoGravacao.OCIOSO
        runCatching { reconhecedor?.destroy() }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        if (!ignorarCallbacks && estado == EstadoGravacao.OUVINDO) {
            aoEstado(EstadoGravacao.OUVINDO)
        }
    }

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        if (ignorarCallbacks || estado != EstadoGravacao.OUVINDO) return
        estado = EstadoGravacao.PROCESSANDO
        aoEstado(EstadoGravacao.PROCESSANDO)
    }

    override fun onError(error: Int) {
        if (ignorarCallbacks) return
        val manual = paradaManual
        estado = EstadoGravacao.OCIOSO
        aoEstado(EstadoGravacao.OCIOSO)
        paradaManual = false
        if (!manual) {
            aoErro(mensagemErroReconhecimento(error))
        }
    }

    override fun onResults(results: Bundle?) {
        if (ignorarCallbacks) return
        val manual = paradaManual
        estado = EstadoGravacao.OCIOSO
        aoEstado(EstadoGravacao.OCIOSO)
        paradaManual = false

        val texto = textoFinal(
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty(),
        )
        when {
            texto != null -> aoFinal(texto)
            !manual -> aoErro("Não entendi o áudio. Tente falar mais perto do microfone.")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        if (ignorarCallbacks) return
        textoParcial(
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty(),
        )?.let(aoParcial)
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
