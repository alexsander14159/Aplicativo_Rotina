package com.rotina.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TelaMetas(
    vm: RotinaViewModel,
    lembreteHora: Int = 20,
    lembreteMinuto: Int = 0,
    onAbrirCalendario: (() -> Unit)? = null,
    onEditarLembrete: (() -> Unit)? = null,
    onTesteLembrete: (() -> Unit)? = null,
    onEditarMetas: (() -> Unit)? = null,
) {
    val metas by vm.metas.collectAsState()
    val submetas by vm.submetas.collectAsState()
    val dia by vm.dia.collectAsState()
    val dataSelecionada by vm.dataSelecionada.collectAsState()
    val hoje = LocalDate.now().toEpochDay()
    val ehHoje = dataSelecionada == hoje
    val folga = dia?.folga == true
    val submetasPorMeta = remember(submetas) { submetas.groupBy { it.metaId } }
    val concluidas = metas.count { it.concluida == true }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Rotina",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.weight(1f),
                    )
                    FilterChip(
                        selected = folga,
                        onClick = { vm.alternarFolga() },
                        label = { Text(if (ehHoje) "Hoje é folga" else "Folga") },
                    )
                    if (onEditarMetas != null) {
                        TextButton(onClick = onEditarMetas) { Text("Metas") }
                    }
                }

                // Navegação por data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = vm::irParaDiaAnterior,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Text("← Anterior")
                    }
                    Text(
                        text = rotuloData(dataSelecionada, hoje) + "  📅",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = onAbrirCalendario != null) {
                                onAbrirCalendario?.invoke()
                            }
                            .padding(vertical = 8.dp),
                    )
                    TextButton(
                        onClick = vm::irParaDiaSeguinte,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Text("Seguinte →")
                    }
                }
                if (!ehHoje) {
                    TextButton(onClick = vm::irParaHoje) { Text("Hoje") }
                }

                // Resumo do dia
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Resumo do dia",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = resumoDoDia(concluidas, metas.size),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            if (folga) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small,
                                ) {
                                    Text(
                                        text = "Folga",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                if (onEditarLembrete != null) {
                    TextButton(onClick = onEditarLembrete) {
                        Text("Lembrete: ${LocalTime.of(lembreteHora, lembreteMinuto)}")
                    }
                }
                if (onTesteLembrete != null) {
                    Button(onClick = onTesteLembrete) {
                        Text("Testar lembrete (debug)")
                    }
                }
                LazyColumn {
                    items(metas, key = { it.id }) { meta ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = meta.concluida == true,
                                    role = Role.Checkbox,
                                    onValueChange = { vm.alternar(meta.id) },
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Checkbox passivo: o clique é do toggleable da linha (sem duplo disparo).
                            Checkbox(checked = meta.concluida == true, onCheckedChange = null)
                            Column {
                                Text(text = meta.nome, style = MaterialTheme.typography.bodyLarge)
                                if (meta.alvoTexto.isNotEmpty()) {
                                    Text(
                                        text = "alvo: ${meta.alvoTexto}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                        submetasPorMeta[meta.id].orEmpty().forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = sub.concluida == true,
                                        role = Role.Checkbox,
                                        onValueChange = { vm.alternarSubmeta(sub.id) },
                                    )
                                    .padding(start = 28.dp)
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = sub.concluida == true, onCheckedChange = null)
                                Text(
                                    text = sub.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
