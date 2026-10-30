package com.rotina.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rotina.data.ChecklistDia
import com.rotina.data.EstadoMeta
import com.rotina.data.RotinaDao
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

    private val alternancia = Mutex()

    /** Alterna a meta na fonte de verdade (banco), imune a toques rápidos em fila. */
    fun alternar(metaId: Long) {
        viewModelScope.launch {
            alternancia.withLock {
                val atual = dao.obter(dataDeHoje, metaId)
                // IMPORTANTE: preservar o id da linha existente — @Upsert resolve conflito
                // de índice único via UPDATE por PRIMARY KEY; item novo usa id=0 (auto).
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
}

/** Fábrica simples: recebe o DAO pronto e monta o ViewModel. */
class RotinaViewModelFactory(private val dao: RotinaDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RotinaViewModel(dao) as T
}
