package com.rotina.ui

/**
 * Próximo estado de marcação a partir do estado atual no dia:
 * sem registro (null) ou desmarcada (false) → marcada (true); marcada (true) → desmarcada (false).
 */
fun proximoEstado(atual: Boolean?): Boolean = atual?.not() ?: true
