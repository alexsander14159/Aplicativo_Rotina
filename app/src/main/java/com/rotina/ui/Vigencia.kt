package com.rotina.ui

/** Item vigente se já começou e ainda não terminou na data consultada. */
fun vigenteNaData(dataInicio: Long, dataFim: Long?, data: Long): Boolean =
    dataInicio <= data && (dataFim == null || dataFim >= data)

/** Exclusão definitiva só quando o item nasceu na data selecionada (ou depois), sem histórico anterior. */
fun podeApagarDefinitivamente(dataInicio: Long, dataSelecionada: Long): Boolean =
    dataInicio >= dataSelecionada
