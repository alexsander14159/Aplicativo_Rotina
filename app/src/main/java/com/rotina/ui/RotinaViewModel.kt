package com.rotina.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rotina.data.ChecklistDia
import com.rotina.data.Dia
import com.rotina.data.EstadoMeta
import com.rotina.data.EstadoSubmeta
import com.rotina.data.ExercicioRegistro
import com.rotina.data.Meta
import com.rotina.data.RegistroTreinoDia
import com.rotina.data.RotinaDao
import com.rotina.data.Submeta
import com.rotina.data.SubmetaDia
import com.rotina.data.TreinoSemana
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(ExperimentalCoroutinesApi::class)
class RotinaViewModel(private val dao: RotinaDao) : ViewModel() {

    /** Data selecionada na navegação; base única para metas, submetas e folga. */
    val dataSelecionada = MutableStateFlow(LocalDate.now().toEpochDay())

    val metas: StateFlow<List<EstadoMeta>> = dataSelecionada
        .flatMapLatest { data -> dao.checklistDoDia(data) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val submetas: StateFlow<List<EstadoSubmeta>> = dataSelecionada
        .flatMapLatest { data -> dao.submetasDoDia(data) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val dia: StateFlow<Dia?> = dataSelecionada
        .flatMapLatest { data -> dao.diaCarregar(data) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    /** Treino da semana correspondente ao dia selecionado (1 = segunda ... 7 = domingo). */
    val treinoDoDia: StateFlow<TreinoSemana?> = dataSelecionada
        .flatMapLatest { epoch ->
            val diaSemana = LocalDate.ofEpochDay(epoch).dayOfWeek.value
            dao.treinoDoDia(diaSemana)
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val todosTreinos: StateFlow<List<TreinoSemana>> = dao.todosTreinos()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    /** Registro do treino realizado na data selecionada (cargas, cardio e alterações). */
    val registroTreinoDoDia: StateFlow<RegistroTreinoDia?> = dataSelecionada
        .flatMapLatest { dao.registroTreinoDoDia(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val exerciciosDoDia: StateFlow<List<ExercicioRegistro>> = dataSelecionada
        .flatMapLatest { dao.exerciciosDoDia(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // Configuração (CRUD): metas e submetas vigentes na data selecionada.
    val metasCfg: StateFlow<List<Meta>> = dataSelecionada
        .flatMapLatest { dao.metasNaData(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val submetasCfg: StateFlow<List<Submeta>> = dataSelecionada
        .flatMapLatest { dao.submetasNaData(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    private val alternancia = Mutex()

    /**
     * Alterna a meta na fonte de verdade (banco), imune a toques rápidos em fila.
     * IMPORTANTE: preservar o id da linha existente — @Upsert resolve conflito de índice
     * único via UPDATE por PRIMARY KEY; item novo usa id=0 (auto). [Ruling Marco 1]
     */
    fun alternar(metaId: Long) {
        val data = dataSelecionada.value
        viewModelScope.launch {
            alternancia.withLock {
                val atual = dao.obter(data, metaId)
                val novo = if (atual != null) {
                    atual.copy(concluida = proximoEstado(atual.concluida))
                } else {
                    ChecklistDia(
                        data = data,
                        metaId = metaId,
                        concluida = proximoEstado(null),
                    )
                }
                dao.salvar(novo)
            }
        }
    }

    /** Alterna a submeta no dia, mesmo padrão da meta (Mutex + copy do id real). */
    fun alternarSubmeta(submetaId: Long) {
        val data = dataSelecionada.value
        viewModelScope.launch {
            alternancia.withLock {
                val atual = dao.submetaObter(data, submetaId)
                val novo = if (atual != null) {
                    atual.copy(concluida = proximoEstado(atual.concluida))
                } else {
                    SubmetaDia(
                        data = data,
                        submetaId = submetaId,
                        concluida = proximoEstado(null),
                    )
                }
                dao.submetaSalvar(novo)
            }
        }
    }

    /** Ligar/desligar a folga do dia (feriado, cochilo, dia esquisito). */
    fun alternarFolga() {
        val data = dataSelecionada.value
        viewModelScope.launch {
            alternancia.withLock {
                val atual = dao.diaObter(data)
                val novo = Dia(
                    data = data,
                    folga = proximoEstado(atual?.folga),
                )
                dao.diaSalvar(novo)
            }
        }
    }

    // ---- Navegação por data ----

    fun irParaDiaAnterior() {
        dataSelecionada.value -= 1
    }

    fun irParaDiaSeguinte() {
        dataSelecionada.value += 1
    }

    fun irParaHoje() {
        dataSelecionada.value = LocalDate.now().toEpochDay()
    }

    /** Seleciona uma data arbitrária (ex.: escolhida no calendário). */
    fun selecionarData(epochDay: Long) {
        dataSelecionada.value = epochDay
    }

    /** Salva (ou substitui) o treino de um dia da semana. */
    fun salvarTreino(diaSemana: Int, titulo: String, descricao: String) {
        viewModelScope.launch {
            dao.salvarTreino(TreinoSemana(diaSemana = diaSemana, titulo = titulo, descricao = descricao))
        }
    }

    // ---- diário do treino realizado ----

    fun salvarRegistroTreino(
        concluido: Boolean,
        alteracoes: String,
        fezCardio: Boolean,
        tipoCardio: String,
        cardioMinutos: Int,
    ) {
        viewModelScope.launch {
            dao.salvarRegistroTreino(
                RegistroTreinoDia(
                    data = dataSelecionada.value,
                    concluido = concluido,
                    alteracoesRotina = alteracoes,
                    fezCardio = fezCardio,
                    tipoCardio = tipoCardio,
                    cardioMinutos = cardioMinutos,
                ),
            )
        }
    }

    fun adicionarExercicio(nome: String, series: Int, reps: String, carga: String) {
        viewModelScope.launch {
            val proximaOrdem = (exerciciosDoDia.value.maxOfOrNull { it.ordem } ?: 0) + 1
            dao.salvarExercicio(
                ExercicioRegistro(
                    data = dataSelecionada.value,
                    nome = nome,
                    series = series,
                    repeticoes = reps,
                    carga = carga,
                    ordem = proximaOrdem,
                ),
            )
        }
    }

    fun apagarExercicio(id: Long) {
        viewModelScope.launch {
            dao.apagarExercicio(id)
        }
    }

    // ---- CRUD da rotina (configuração) ----

    /** Cria meta nova no fim da lista, vigente a partir da data selecionada. */
    fun criarMeta(nome: String, alvoTexto: String) {
        val data = dataSelecionada.value
        viewModelScope.launch {
            alternancia.withLock {
                val proxima = (metasCfg.value.maxOfOrNull { it.ordem } ?: 0) + 1
                dao.metaSalvar(
                    Meta(
                        nome = nome,
                        ordem = proxima,
                        alvoTexto = alvoTexto.trim(),
                        dataInicio = data,
                    ),
                )
            }
        }
    }

    /** Salva edições (nome/alvo); a entidade da tela traz o id real (Ruling upsert-PK). */
    fun atualizarMeta(meta: Meta) {
        viewModelScope.launch {
            alternancia.withLock { dao.metaSalvar(meta) }
        }
    }

    /** Apaga a meta se ela nasceu nesta data; senão encerra a vigência preservando o histórico. */
    fun apagarMeta(metaId: Long) {
        val data = dataSelecionada.value
        viewModelScope.launch {
            alternancia.withLock {
                val meta = dao.obterMeta(metaId) ?: return@withLock
                if (podeApagarDefinitivamente(meta.dataInicio, data)) {
                    dao.apagarMetaComDependencias(meta)
                } else {
                    dao.encerrarMeta(meta.id, data - 1)
                }
            }
        }
    }

    /** Cria submeta no fim da lista da meta, vigente a partir da data selecionada. */
    fun criarSubmeta(metaId: Long, nome: String) {
        val data = dataSelecionada.value
        viewModelScope.launch {
            alternancia.withLock {
                val proxima = (submetasCfg.value.filter { it.metaId == metaId }.maxOfOrNull { it.ordem } ?: 0) + 1
                dao.submetaSalvarCfg(
                    Submeta(
                        metaId = metaId,
                        nome = nome.trim(),
                        ordem = proxima,
                        dataInicio = data,
                    ),
                )
            }
        }
    }

    /** Apaga a submeta se ela nasceu nesta data; senão encerra a vigência preservando o histórico. */
    fun apagarSubmeta(submeta: Submeta) {
        val data = dataSelecionada.value
        viewModelScope.launch {
            alternancia.withLock {
                if (podeApagarDefinitivamente(submeta.dataInicio, data)) {
                    dao.apagarSubmetaComMarcas(submeta)
                } else {
                    dao.encerrarSubmeta(submeta.id, data - 1)
                }
            }
        }
    }

    /** Move a meta uma posição (deslocamento -1 ou +1), reescrevendo só o que mudou. */
    fun moverMeta(metaId: Long, deslocamento: Int) {
        val data = dataSelecionada.value
        viewModelScope.launch {
            alternancia.withLock {
                val metas = dao.metasSincronas()
                    .filter { vigenteNaData(it.dataInicio, it.dataFim, data) }
                    .sortedBy { it.ordem }
                val de = metas.indexOfFirst { it.id == metaId }
                if (de < 0) return@withLock
                val para = (de + deslocamento).coerceIn(0, metas.size - 1)
                if (de == para) return@withLock
                val novas = ordensAposTrocar(metas.map { it.ordem }, de, para)
                metas.forEachIndexed { i, m ->
                    if (m.ordem != novas[i]) dao.metaSalvar(m.copy(ordem = novas[i]))
                }
            }
        }
    }
}

/** Fábrica simples: recebe o DAO pronto e monta o ViewModel. */
class RotinaViewModelFactory(private val dao: RotinaDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RotinaViewModel(dao) as T
}
