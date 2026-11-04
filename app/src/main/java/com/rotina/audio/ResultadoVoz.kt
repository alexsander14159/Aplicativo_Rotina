package com.rotina.audio

import android.speech.SpeechRecognizer

/** Melhor resultado parcial (o primeiro não vazio) recebido do reconhecedor. */
fun textoParcial(resultados: List<String>): String? =
    resultados.firstOrNull { it.isNotBlank() }?.trim()

/** Melhor resultado final (o primeiro não vazio) recebido do reconhecedor. */
fun textoFinal(resultados: List<String>): String? =
    resultados.firstOrNull { it.isNotBlank() }?.trim()

/** Converte os códigos do SpeechRecognizer em uma mensagem curta para a tela. */
fun mensagemErroReconhecimento(codigo: Int): String = when (codigo) {
    SpeechRecognizer.ERROR_AUDIO -> "Erro ao capturar o áudio."
    SpeechRecognizer.ERROR_CLIENT -> "O reconhecimento foi interrompido. Tente novamente."
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissão de microfone negada."
    SpeechRecognizer.ERROR_NETWORK -> "Sem conexão para o reconhecimento de voz."
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tempo de rede esgotado. Tente novamente."
    SpeechRecognizer.ERROR_NO_MATCH -> "Não entendi o áudio. Tente falar mais perto do microfone."
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "O reconhecedor está ocupado. Aguarde e tente novamente."
    SpeechRecognizer.ERROR_SERVER -> "O serviço de voz retornou um erro."
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nenhuma fala detectada. Tente novamente."
    SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "Muitas tentativas de voz. Aguarde um pouco e tente novamente."
    SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "A conexão com o serviço de voz caiu. Tente novamente."
    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "O reconhecimento de voz não suporta português neste aparelho."
    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "O idioma português não está disponível no reconhecimento de voz."
    SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT -> "Não foi possível verificar o suporte de voz neste aparelho."
    SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS -> "Não foi possível ouvir o serviço de voz."
    else -> "Não foi possível reconhecer a fala (código $codigo)."
}
