package com.rotina.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rotina.audio.EstadoGravacao
import com.rotina.data.NotaDia

/** Card do diário e dos momentos: lista rápida, campo editável, gravação nativa e status. */
@Composable
fun DiarioCard(
    texto: String,
    estado: EstadoGravacao,
    mensagem: String?,
    disponivel: Boolean,
    pendente: Boolean,
    temDiarioSalvo: Boolean,
    notas: List<NotaDia>,
    onApagarNota: (Long) -> Unit,
    onAnotarMomento: () -> Unit,
    onTextoChange: (String) -> Unit,
    onGravar: () -> Unit,
    onParar: () -> Unit,
    onSalvar: () -> Unit,
) {
    val ocupado = estado != EstadoGravacao.OCIOSO
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Momentos & Diário do Dia",
                style = MaterialTheme.typography.titleSmall,
            )

            if (notas.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    notas.forEach { nota ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    text = nota.hora,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = nota.texto,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = { onApagarNota(nota.id) },
                                enabled = !ocupado,
                                modifier = Modifier.semantics {
                                    contentDescription = "Apagar momento"
                                },
                            ) {
                                Text("🗑️")
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Nenhum momento registrado ainda.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = onAnotarMomento,
                enabled = !ocupado,
            ) {
                Text("+ Anotar Momento")
            }

            when (estado) {
                EstadoGravacao.OUVINDO -> Text(
                    text = "🔴 Ouvindo... Fale o seu diário",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                EstadoGravacao.PROCESSANDO -> Text(
                    text = "⏳ Processando o áudio...",
                    style = MaterialTheme.typography.labelLarge,
                )
                EstadoGravacao.OCIOSO -> Unit
            }
            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !ocupado,
                label = { Text("Como foi o seu dia?") },
                minLines = 3,
            )
            if (pendente && !ocupado) {
                Text(
                    text = "Alterações não salvas. Salve antes de mudar de data.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            mensagem?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (estado) {
                    EstadoGravacao.OUVINDO -> Button(onClick = onParar) { Text("Parar") }
                    EstadoGravacao.PROCESSANDO -> Button(onClick = {}, enabled = false) {
                        Text("Processando...")
                    }
                    EstadoGravacao.OCIOSO -> Button(
                        onClick = onGravar,
                        enabled = disponivel,
                    ) {
                        Text("🎙️ Gravar Diário")
                    }
                }
                TextButton(
                    onClick = onSalvar,
                    enabled = !ocupado && (texto.isNotBlank() || temDiarioSalvo),
                ) {
                    Text("Salvar")
                }
            }
            if (!disponivel) {
                Text(
                    text = "Reconhecimento de voz indisponível neste aparelho; digite o diário.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
