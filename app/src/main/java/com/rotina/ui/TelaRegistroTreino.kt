package com.rotina.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Diário do treino realizado na data selecionada: cargas, séries, cardio e observações. */
@Composable
fun TelaRegistroTreino(
    vm: RotinaViewModel,
    onVoltar: () -> Unit,
) {
    val registro by vm.registroTreinoDoDia.collectAsState()
    val exercicios by vm.exerciciosDoDia.collectAsState()
    val dataSelecionada by vm.dataSelecionada.collectAsState()
    val data = LocalDate.ofEpochDay(dataSelecionada)

    var concluido by rememberSaveable(registro?.concluido) {
        mutableStateOf(registro?.concluido == true)
    }
    var alteracoes by rememberSaveable(registro?.alteracoesRotina) {
        mutableStateOf(registro?.alteracoesRotina.orEmpty())
    }
    var fezCardio by rememberSaveable(registro?.fezCardio) {
        mutableStateOf(registro?.fezCardio == true)
    }
    var tipoCardio by rememberSaveable(registro?.tipoCardio) {
        mutableStateOf(registro?.tipoCardio.orEmpty())
    }
    var minutosTexto by rememberSaveable(registro?.cardioMinutos) {
        mutableStateOf(registro?.cardioMinutos?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    fun salvar() {
        vm.salvarRegistroTreino(
            concluido = concluido,
            alteracoes = alteracoes.trim(),
            fezCardio = fezCardio,
            tipoCardio = if (fezCardio) tipoCardio.trim() else "",
            cardioMinutos = if (fezCardio) minutosTexto.toIntOrNull() ?: 0 else 0,
        )
        onVoltar()
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Registro do Treino",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = data.format(FORMATO_DATA),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    TextButton(onClick = onVoltar) { Text("Voltar") }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .toggleable(
                                            value = concluido,
                                            role = Role.Checkbox,
                                            onValueChange = { concluido = it },
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(checked = concluido, onCheckedChange = null)
                                    Text("Treino do dia concluído")
                                }
                                OutlinedTextField(
                                    value = alteracoes,
                                    onValueChange = { alteracoes = it },
                                    label = { Text("Alterações na rotina / Observações") },
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .toggleable(
                                            value = fezCardio,
                                            role = Role.Checkbox,
                                            onValueChange = { fezCardio = it },
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(checked = fezCardio, onCheckedChange = null)
                                    Text("Fez cardio hoje?")
                                }
                                if (fezCardio) {
                                    OutlinedTextField(
                                        value = tipoCardio,
                                        onValueChange = { tipoCardio = it },
                                        label = {
                                            Text("Tipo de cardio (ex.: Esteira, Bicicleta, Caminhada)")
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    OutlinedTextField(
                                        value = minutosTexto,
                                        onValueChange = { novo ->
                                            if (novo.all(Char::isDigit)) minutosTexto = novo
                                        },
                                        label = { Text("Duração em minutos") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Exercícios realizados",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Button(onClick = { mostrarDialogo = true }) { Text("Adicionar") }
                        }
                    }

                    if (exercicios.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhum exercício anotado ainda.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    items(exercicios, key = { it.id }) { exercicio ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = resumoExercicio(
                                        nome = exercicio.nome,
                                        series = exercicio.series,
                                        repeticoes = exercicio.repeticoes,
                                        carga = exercicio.carga,
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = { vm.apagarExercicio(exercicio.id) }) {
                                    Text("Apagar")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { salvar() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Salvar e Voltar")
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoAdicionarExercicio(
            onAdicionar = { nome, series, reps, carga ->
                vm.adicionarExercicio(nome, series, reps, carga)
                mostrarDialogo = false
            },
            onFechar = { mostrarDialogo = false },
        )
    }
}

@Composable
private fun DialogoAdicionarExercicio(
    onAdicionar: (nome: String, series: Int, reps: String, carga: String) -> Unit,
    onFechar: () -> Unit,
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var seriesTexto by rememberSaveable { mutableStateOf("") }
    var reps by rememberSaveable { mutableStateOf("") }
    var carga by rememberSaveable { mutableStateOf("") }
    val series = seriesTexto.toIntOrNull() ?: 0
    val valido = nome.isNotBlank() && series > 0

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Adicionar exercício") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do exercício") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = seriesTexto,
                    onValueChange = { novo ->
                        if (novo.all(Char::isDigit)) seriesTexto = novo
                    },
                    label = { Text("Séries") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Repetições (ex.: 10-10-8)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = carga,
                    onValueChange = { carga = it },
                    label = { Text("Carga/Peso (ex.: 25kg cada lado)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido,
                onClick = { onAdicionar(nome.trim(), series, reps.trim(), carga.trim()) },
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onFechar) { Text("Cancelar") }
        },
    )
}
