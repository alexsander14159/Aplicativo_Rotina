package com.rotina.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun TelaMetas(
    vm: RotinaViewModel,
    onTesteLembrete: (() -> Unit)? = null,
) {
    val metas by vm.metas.collectAsState()
    val submetas by vm.submetas.collectAsState()
    val dia by vm.dia.collectAsState()
    val rotuloData = LocalDate.ofEpochDay(vm.dataDeHoje).toString()
    val folga = dia?.folga == true
    val submetasPorMeta = remember(submetas) { submetas.groupBy { it.metaId } }

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
                        label = { Text("Hoje é folga") },
                    )
                }
                Text(
                    text = "Metas de hoje — $rotuloData" + if (folga) " · folga 😌" else "",
                    style = MaterialTheme.typography.titleMedium,
                )
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
