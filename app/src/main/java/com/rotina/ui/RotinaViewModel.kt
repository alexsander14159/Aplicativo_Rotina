package com.rotina.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rotina.data.ChecklistDia
import com.rotina.data.Dia
import com.rotina.data.EstadoMeta
import com.rotina.data.EstadoSubmeta
import com.rotina.data.RotinaDao
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
}

/** Fábrica simples: recebe o DAO pronto e monta o ViewModel. */
class RotinaViewModelFactory(private val dao: RotinaDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RotinaViewModel(dao) as T
}
