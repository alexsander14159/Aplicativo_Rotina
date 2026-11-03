package com.rotina.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rotina.data.TreinoSemana

private val NOMES_DIAS = listOf(
    "Segunda-feira",
    "Terça-feira",
    "Quarta-feira",
    "Quinta-feira",
    "Sexta-feira",
    "Sábado",
    "Domingo",
)

/** Edição dos 7 treinos da semana (1 = segunda ... 7 = domingo). */
@Composable
fun TelaTreinosEdit(
    vm: RotinaViewModel,
    onVoltar: () -> Unit,
) {
    val treinos by vm.todosTreinos.collectAsState()
    val porDia = remember(treinos) { treinos.associateBy { it.diaSemana } }

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
                        text = "Treinos da semana",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items((1..7).toList()) { diaSemana ->
                        CardTreino(
                            diaSemana = diaSemana,
                            treino = porDia[diaSemana],
                            onSalvar = { titulo, descricao ->
                                vm.salvarTreino(diaSemana, titulo, descricao)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardTreino(
    diaSemana: Int,
    treino: TreinoSemana?,
    onSalvar: (titulo: String, descricao: String) -> Unit,
) {
    // Reinicia os campos quando o valor salvo no banco muda (carga inicial ou novo "Salvar").
    var titulo by rememberSaveable(diaSemana, treino?.titulo) {
        mutableStateOf(treino?.titulo.orEmpty())
    }
    var descricao by rememberSaveable(diaSemana, treino?.descricao) {
        mutableStateOf(treino?.descricao.orEmpty())
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = NOMES_DIAS[diaSemana - 1],
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título (ex.: Peito e Tríceps, Descanso)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição (exercícios ou notas)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = { onSalvar(titulo.trim(), descricao.trim()) }) {
                Text("Salvar")
            }
        }
    }
}
