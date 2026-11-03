package com.rotina.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rotina.data.Meta
import com.rotina.data.Submeta

@Composable
fun TelaMetasEdit(
    vm: RotinaViewModel,
    onVoltar: () -> Unit,
) {
    val metas by vm.metasCfg.collectAsState()
    val submetas by vm.submetasCfg.collectAsState()
    val submetasPorMeta = remember(submetas) { submetas.groupBy { it.metaId } }

    var dialogoNovaMeta by remember { mutableStateOf(false) }
    var metaEmEdicao by remember { mutableStateOf<Meta?>(null) }
    var metaApagando by remember { mutableStateOf<Meta?>(null) }
    var metaNewSubmeta by remember { mutableStateOf<Meta?>(null) }

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
                    TextButton(onClick = onVoltar) { Text("← Voltar") }
                    Text(
                        text = "Metas",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Button(onClick = { dialogoNovaMeta = true }) { Text("Nova") }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(metas, key = { it.id }) { meta ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = meta.nome,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                if (meta.alvoTexto.isNotEmpty()) {
                                    Text(
                                        text = "alvo: ${meta.alvoTexto}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            TextButton(onClick = { vm.moverMeta(meta.id, -1) }) { Text("↑") }
                            TextButton(onClick = { vm.moverMeta(meta.id, +1) }) { Text("↓") }
                            TextButton(onClick = { metaEmEdicao = meta }) { Text("✏️") }
                            TextButton(onClick = { metaApagando = meta }) { Text("×") }
                        }
                        submetasPorMeta[meta.id].orEmpty().forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "· ${sub.nome}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = { vm.apagarSubmeta(sub) }) { Text("×") }
                            }
                        }
                        TextButton(
                            onClick = { metaNewSubmeta = meta },
                            modifier = Modifier.padding(start = 12.dp),
                        ) { Text("+ submeta") }
                    }
                }
            }
        }
    }

    if (dialogoNovaMeta) {
        DialogoMeta(
            titulo = "Nova meta",
            nomeInicial = "",
            alvoInicial = "",
            onSalvar = { nome, alvo ->
                vm.criarMeta(nome, alvo)
                dialogoNovaMeta = false
            },
            onFechar = { dialogoNovaMeta = false },
        )
    }
    if (metaEmEdicao != null) {
        val meta = metaEmEdicao!!
        DialogoMeta(
            titulo = "Editar meta",
            nomeInicial = meta.nome,
            alvoInicial = meta.alvoTexto,
            onSalvar = { nome, alvo ->
                vm.atualizarMeta(meta.copy(nome = nome.trim(), alvoTexto = alvo.trim()))
                metaEmEdicao = null
            },
            onFechar = { metaEmEdicao = null },
        )
    }
    if (metaApagando != null) {
        val meta = metaApagando!!
        AlertDialog(
            onDismissRequest = { metaApagando = null },
            title = { Text("Apagar meta") },
            text = { Text("Apagar \"${meta.nome}\" e todas as submetas/marcações dela?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.apagarMeta(meta.id)
                    metaApagando = null
                }) { Text("Apagar") }
            },
            dismissButton = {
                TextButton(onClick = { metaApagando = null }) { Text("Cancelar") }
            },
        )
    }
    if (metaNewSubmeta != null) {
        val meta = metaNewSubmeta!!
        DialogoTexto(
            titulo = "Nova submeta de \"${meta.nome}\"",
            label = "Nome da submeta",
            textoInicial = "",
            onSalvar = { nome ->
                vm.criarSubmeta(meta.id, nome)
                metaNewSubmeta = null
            },
            onFechar = { metaNewSubmeta = null },
        )
    }
}

/** Diálogo de nome + alvo (criar/editar meta). */
@Composable
private fun DialogoMeta(
    titulo: String,
    nomeInicial: String,
    alvoInicial: String,
    onSalvar: (nome: String, alvoTexto: String) -> Unit,
    onFechar: () -> Unit,
) {
    var nome by remember { mutableStateOf(nomeInicial) }
    var alvo by remember { mutableStateOf(alvoInicial) }
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da meta") },
                )
                OutlinedTextField(
                    value = alvo,
                    onValueChange = { alvo = it },
                    label = { Text("Alvo do dia (ex.: 2h, 8 copos)") },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (nome.isNotBlank()) onSalvar(nome, alvo) },
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } },
    )
}

/** Diálogo de um campo só (criar submeta). */
@Composable
private fun DialogoTexto(
    titulo: String,
    label: String,
    textoInicial: String,
    onSalvar: (String) -> Unit,
    onFechar: () -> Unit,
) {
    var texto by remember { mutableStateOf(textoInicial) }
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text(label) },
            )
        },
        confirmButton = {
            TextButton(onClick = { if (texto.isNotBlank()) onSalvar(texto) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } },
    )
}
