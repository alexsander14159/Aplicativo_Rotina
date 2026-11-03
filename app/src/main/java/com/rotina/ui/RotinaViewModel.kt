package com.rotina.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rotina.data.ChecklistDia
import com.rotina.data.Dia
import com.rotina.data.EstadoMeta
import com.rotina.data.EstadoSubmeta
import com.rotina.data.Meta
import com.rotina.data.RotinaDao
import com.rotina.data.Submeta
import com.rotina.data.SubmetaDia
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RotinaViewModel(private val dao: RotinaDao) : ViewModel() {

    /** Data do aparelho na criação do ViewModel (uma jornada = um objeto do VM). */
    val dataDeHoje: Long = LocalDate.now().toEpochDay()

    val metas: StateFlow<List<EstadoMeta>> = dao.checklistDoDia(dataDeHoje)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val submetas: StateFlow<List<EstadoSubmeta>> = dao.submetasDoDia(dataDeHoje)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val dia: StateFlow<Dia?> = dao.diaCarregar(dataDeHoje)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    // Configuração (CRUD): lista completa de metas e submetas, sem estado do dia.
    val metasCfg: StateFlow<List<Meta>> = dao.metas()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val submetasCfg: StateFlow<List<Submeta>> = dao.submetasCfg()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    private val alternancia = Mutex()

    /**
     * Alterna a meta na fonte de verdade (banco), imune a toques rápidos em fila.
     * IMPORTANTE: preservar o id da linha existente — @Upsert resolve conflito de índice
     * único via UPDATE por PRIMARY KEY; item novo usa id=0 (auto). [Ruling Marco 1]
     */
    fun alternar(metaId: Long) {
        viewModelScope.launch {
            alternancia.withLock {
                val atual = dao.obter(dataDeHoje, metaId)
                val novo = if (atual != null) {
                    atual.copy(concluida = proximoEstado(atual.concluida))
                } else {
                    ChecklistDia(
                        data = dataDeHoje,
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
        viewModelScope.launch {
            alternancia.withLock {
                val atual = dao.submetaObter(dataDeHoje, submetaId)
                val novo = if (atual != null) {
                    atual.copy(concluida = proximoEstado(atual.concluida))
                } else {
                    SubmetaDia(
                        data = dataDeHoje,
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
        viewModelScope.launch {
            alternancia.withLock {
                val atual = dao.diaObter(dataDeHoje)
                val novo = Dia(
                    data = dataDeHoje,
                    folga = proximoEstado(atual?.folga),
                )
                dao.diaSalvar(novo)
            }
        }
    }

    // ---- CRUD da rotina (configuração) ----

    /** Cria meta nova no fim da lista com o alvo do dia em texto. */
    fun criarMeta(nome: String, alvoTexto: String) {
        viewModelScope.launch {
            alternancia.withLock {
                dao.metaSalvar(
                    Meta(nome = nome, ordem = dao.maiorOrdemMeta() + 1, alvoTexto = alvoTexto.trim()),
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

    /** Apaga a meta + submetas + todas as marcações (transação) e compacta as ordens. */
    fun apagarMeta(metaId: Long) {
        viewModelScope.launch {
            alternancia.withLock {
                val meta = dao.obterMeta(metaId) ?: return@withLock
                dao.apagarMetaComDependencias(meta)
                dao.metasSincronas().forEachIndexed { i, m ->
                    if (m.ordem != i + 1) dao.metaSalvar(m.copy(ordem = i + 1))
                }
            }
        }
    }

    /** Cria submeta no fim da lista da meta. */
    fun criarSubmeta(metaId: Long, nome: String) {
        viewModelScope.launch {
            alternancia.withLock {
                dao.submetaSalvarCfg(
                    Submeta(metaId = metaId, nome = nome.trim(), ordem = dao.maiorOrdemSubmeta(metaId) + 1),
                )
            }
        }
    }

    /** Apaga a submeta + marcações (transação) e compacta as ordens da meta. */
    fun apagarSubmeta(submeta: Submeta) {
        viewModelScope.launch {
            alternancia.withLock {
                dao.apagarSubmetaComMarcas(submeta)
                dao.submetasDaMeta(submeta.metaId).forEachIndexed { i, s ->
                    if (s.ordem != i + 1) dao.submetaSalvarCfg(s.copy(ordem = i + 1))
                }
            }
        }
    }

    /** Move a meta uma posição (deslocamento -1 ou +1), reescrevendo só o que mudou. */
    fun moverMeta(metaId: Long, deslocamento: Int) {
        viewModelScope.launch {
            alternancia.withLock {
                val metas = dao.metasSincronas()
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
