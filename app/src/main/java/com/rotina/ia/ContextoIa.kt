package com.rotina.ia

import com.rotina.data.EstadoMeta
import com.rotina.data.EstadoSubmeta
import com.rotina.data.ExercicioRegistro
import com.rotina.data.RegistroTreinoDia
import com.rotina.data.RotinaDao
import com.rotina.data.TreinoSemana
import kotlinx.coroutines.flow.first
import java.time.LocalDate

data class ContextoDia(
    val data: Long, // epochDay
    val folga: Boolean,
    val metas: List<String>, // "- Meta (alvo): Feita" | "Pendente"
    val submetas: List<String>, // igual, por submeta
    val treinoPlanejado: String?, // titulo+descricao de treinos_semana do diaSemana
    val treinoRealizado: Boolean?,
    val exercicios: List<String>, // "Supino Reto: 4 série(s) x 10 reps @ 30 kg"
    val cardio: String?, // "20 min Esteira"
    val observacoes: String?,
    val diarioTexto: String, // diário de voz do dia (pode ser vazia)
    val perfil: String, // PerfilIa.obter(...)
    val notasRapidas: List<String> = emptyList(), // "09:30: texto", em ordem ascendente
)

/** Coleta o contexto do dia. Chame em Dispatchers.IO (faz várias leituras pontuais). */
suspend fun coletar(dao: RotinaDao, data: Long, perfil: String): ContextoDia {
    val metas = dao.checklistDoDia(data).first().map(::formatarMeta)
    val submetas = dao.submetasDoDia(data).first().map(::formatarSubmeta)
    val dia = dao.diaObter(data)
    val diario = dao.diarioDoDia(data).first()
    val diaSemana = LocalDate.ofEpochDay(data).dayOfWeek.value
    val treino = dao.treinoDoDia(diaSemana).first()
    val registro = dao.registroTreinoDoDia(data).first()
    val exercicios = dao.exerciciosDoDia(data).first().map(::formatarExercicio)
    val notas = dao.notasDoDia(data).first().map { "${it.hora}: ${it.texto}" }

    return ContextoDia(
        data = data,
        folga = dia?.folga == true,
        metas = metas,
        submetas = submetas,
        treinoPlanejado = formatarTreino(treino),
        treinoRealizado = registro?.concluido,
        exercicios = exercicios,
        cardio = formatarCardio(registro),
        observacoes = registro?.alteracoesRotina?.trim()?.takeIf { it.isNotEmpty() },
        diarioTexto = diario?.texto?.trim().orEmpty(),
        perfil = perfil,
        notasRapidas = notas,
    )
}

/** Resumo determinístico do dia, uma linha por campo. O perfil é enviado separadamente no system. */
fun resumoTexto(c: ContextoDia): String {
    val linhas = mutableListOf<String>()
    if (c.folga) linhas += "Hoje é folga."
    if (!c.folga && !temRegistro(c)) linhas += "Nenhum registro no dia."

    if (c.metas.isNotEmpty()) {
        linhas += "Metas:"
        linhas += c.metas
    }
    if (c.submetas.isNotEmpty()) {
        linhas += "Submetas:"
        linhas += c.submetas
    }
    c.treinoPlanejado?.let { linhas += "Treino planejado: $it" }
    c.treinoRealizado?.let { linhas += "Treino realizado: ${if (it) "Sim" else "Não"}" }
    if (c.exercicios.isNotEmpty()) {
        linhas += "Exercícios:"
        linhas += c.exercicios
    }
    c.cardio?.let { linhas += "Cardio: $it" }
    c.observacoes?.let { linhas += "Observações: $it" }
    if (c.diarioTexto.isNotBlank()) linhas += "Diário: ${c.diarioTexto}"
    if (c.notasRapidas.isNotEmpty()) {
        linhas += "Momentos registrados ao longo do dia:"
        linhas += c.notasRapidas
    }

    return linhas.joinToString("\n")
}

private fun temRegistro(c: ContextoDia): Boolean =
    c.metas.any { it.endsWith(": Feita") } ||
        c.submetas.any { it.endsWith(": Feita") } ||
        c.treinoRealizado != null ||
        c.exercicios.isNotEmpty() ||
        c.cardio != null ||
        c.observacoes != null ||
        c.diarioTexto.isNotBlank() ||
        c.notasRapidas.isNotEmpty()

private fun formatarMeta(m: EstadoMeta): String {
    val alvo = m.alvoTexto.trim().takeIf { it.isNotEmpty() }?.let { " ($it)" }.orEmpty()
    val estado = if (m.concluida == true) "Feita" else "Pendente"
    return "- ${m.nome}$alvo: $estado"
}

private fun formatarSubmeta(s: EstadoSubmeta): String {
    val estado = if (s.concluida == true) "Feita" else "Pendente"
    return "- ${s.nome}: $estado"
}

private fun formatarTreino(t: TreinoSemana?): String? {
    if (t == null) return null
    val titulo = t.titulo.trim()
    val descricao = t.descricao.trim()
    return when {
        titulo.isNotEmpty() && descricao.isNotEmpty() -> "$titulo — $descricao"
        titulo.isNotEmpty() -> titulo
        descricao.isNotEmpty() -> descricao
        else -> null
    }
}

private fun formatarExercicio(e: ExercicioRegistro): String {
    val base = "${e.nome}: ${e.series} série(s) x ${e.repeticoes} reps"
    return if (e.carga.isNotBlank()) "$base @ ${e.carga.trim()}" else base
}

private fun formatarCardio(r: RegistroTreinoDia?): String? {
    if (r == null || !r.fezCardio) return null
    val tipo = r.tipoCardio.trim()
    return if (tipo.isEmpty()) "${r.cardioMinutos} min" else "${r.cardioMinutos} min $tipo"
}
