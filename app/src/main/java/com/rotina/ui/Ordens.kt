package com.rotina.ui

/**
 * Lógica pura de reordenação das metas (testada em JVM — OrdemTest).
 * Vale também para as submetas (mesma semântica de lista ordenada).
 */

/** Move o item da posição `de` para a posição `para`, deslocando o resto. */
fun ordensAposTrocar(ordens: List<Int>, de: Int, para: Int): List<Int> {
    val mutavel = ordens.toMutableList()
    val item = mutavel.removeAt(de)
    mutavel.add(para.coerceIn(0, mutavel.size), item)
    return mutavel
}

/** Remove o item da posição `pos` e compacta as ordens restantes para 1..n. */
fun ordensAposRemover(ordens: List<Int>, pos: Int): List<Int> =
    ordens.filterIndexed { i, _ -> i != pos }.mapIndexed { i, _ -> i + 1 }
