package com.rotina.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rotina.audio.EstadoGravacao

/** Card do diário: campo editável, gravação nativa e status da última ação. */
@Composable
fun DiarioCard(
    texto: String,
    estado: EstadoGravacao,
    mensagem: String?,
    disponivel: Boolean,
    pendente: Boolean,
    temDiarioSalvo: Boolean,
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
                text = "Diário do Dia",
                style = MaterialTheme.typography.titleSmall,
            )
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
