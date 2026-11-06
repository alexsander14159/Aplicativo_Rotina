package com.rotina.ia

import android.content.Context

/**
 * Texto que descreve quem o usuário é na visão do Jarvis: estilo, dificuldades,
 * o que funciona para ele. Começa com a persona padrão e é EVOLUÍDO pela própria IA
 * (a escrita pela IA chega na 6.3; aqui só há leitura e salvar).
 * Nunca registre o conteúdo da chave em logs; o perfil não é segredo, a chave sim.
 */
object PerfilIa {
    private const val KEY_PERFIL = "perfil_jarvis"

    const val PERFIL_PADRAO: String = "Você é o Jarvis, mentor de rotina em pt-BR: direto, motivador e honesto. " +
        "Comemora vitórias reais, aponta falhas sem enrolação e dá um conselho claro para o dia seguinte. " +
        "Conheça o dia do usuário pelos dados abaixo antes de responder."

    fun obter(context: Context): String =
        context.getSharedPreferences("rotina_ia_prefs", Context.MODE_PRIVATE)
            .getString(KEY_PERFIL, "")?.trim().orEmpty().ifEmpty { PERFIL_PADRAO }

    fun salvar(context: Context, texto: String) {
        context.getSharedPreferences("rotina_ia_prefs", Context.MODE_PRIVATE)
            .edit().putString(KEY_PERFIL, texto.trim()).apply()
    }
}
