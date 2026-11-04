package com.rotina.ia

import android.content.Context

/**
 * Guarda a chave do Gemini localmente em SharedPreferences privado.
 * Nunca registre o conteúdo da chave em logs, telas ou no Git.
 */
object IaConfig {
    private const val PREFS = "rotina_ia_prefs"
    private const val KEY_GEMINI = "gemini_api_key"

    fun obterChave(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_GEMINI, "") ?: ""
    }

    fun salvarChave(context: Context, chave: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GEMINI, chave.trim())
            .apply()
    }
}
