package com.rotina.ia

import com.rotina.data.MensagemIa

data class MensagemPrompt(val papel: String, val texto: String) // "system" | "user" | "model"

/** Mantém as [limite] mensagens mais recentes, sempre em ordem ascendente. */
fun historicoParaEnvio(mensagens: List<MensagemIa>, limite: Int = 30): List<MensagemIa> {
    if (limite <= 0) return emptyList()
    return mensagens
        .sortedWith(compareBy<MensagemIa> { it.criadoEm }.thenBy { it.id })
        .takeLast(limite)
}

/** Monta o prompt do Jarvis: system com perfil/contexto, histórico e pergunta ou briefing. */
fun montar(contexto: ContextoDia, historico: List<MensagemIa>, pergunta: String?): List<MensagemPrompt> {
    val prompts = mutableListOf<MensagemPrompt>()
    prompts += MensagemPrompt("system", montarSystem(contexto))

    historico.forEach { mensagem ->
        when (mensagem.origem) {
            "USER" -> prompts += MensagemPrompt("user", mensagem.conteudo)
            "ASSISTANT" -> prompts += MensagemPrompt("model", mensagem.conteudo)
        }
    }

    if (pergunta != null) {
        prompts += MensagemPrompt("user", pergunta)
    } else {
        prompts += MensagemPrompt(
            "user",
            "Gere o briefing do meu dia com base no contexto acima: comemore as vitórias, " +
                "aponte as falhas e dê um conselho claro para o dia seguinte.",
        )
    }

    return prompts
}

private fun montarSystem(contexto: ContextoDia): String {
    val perfil = contexto.perfil.trim().ifEmpty { PerfilIa.PERFIL_PADRAO }
    val partes = mutableListOf(PerfilIa.PERFIL_PADRAO)
    if (perfil != PerfilIa.PERFIL_PADRAO) {
        partes += "Perfil adaptativo do usuário:\n$perfil"
    }
    partes += "Contexto do dia:\n${resumoTexto(contexto)}"
    return partes.joinToString("\n\n")
}
