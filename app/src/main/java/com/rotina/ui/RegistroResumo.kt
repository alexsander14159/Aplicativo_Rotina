package com.rotina.ui

/** Linha amigável de um exercício realizado (ex.: "Supino — 4 séries • 10 reps • 30 kg"). */
fun resumoExercicio(nome: String, series: Int, repeticoes: String, carga: String): String {
    val detalhe = buildList {
        if (series > 0) add("$series ${if (series == 1) "série" else "séries"}")
        if (repeticoes.isNotBlank()) add("${repeticoes.trim()} reps")
        if (carga.isNotBlank()) add(carga.trim())
    }.joinToString(" • ")
    val nomeLimpo = nome.trim()
    return if (detalhe.isEmpty()) nomeLimpo else "$nomeLimpo — $detalhe"
}

/** Resumo do cardio para o card do dia; null quando não houve cardio. */
fun resumoCardio(fezCardio: Boolean, tipo: String, minutos: Int): String? {
    if (!fezCardio) return null
    val duracao = if (minutos > 0) "$minutos min" else "duração não informada"
    val tipoLimpo = tipo.trim()
    return if (tipoLimpo.isEmpty()) "Cardio: $duracao" else "Cardio: $duracao ($tipoLimpo)"
}

/** Contagem de exercícios anotados no dia; null quando não há nenhum. */
fun resumoExerciciosAnotados(quantidade: Int): String? {
    if (quantidade <= 0) return null
    return "Exercícios: $quantidade ${if (quantidade == 1) "anotado" else "anotados"}"
}
