package com.rotina.ui

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.rotina.audio.EstadoGravacao
import com.rotina.audio.GravadorVoz

/** Diálogo para digitar ou ditar um momento rápido do dia. */
@Composable
fun AnotarMomentoDialog(
    onDismiss: () -> Unit,
    onSalvar: (String) -> Unit,
) {
    val contexto = LocalContext.current
    var texto by rememberSaveable { mutableStateOf("") }
    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }
    var estado by remember { mutableStateOf(EstadoGravacao.OCIOSO) }
    var textoBase by remember { mutableStateOf("") }

    fun juntar(base: String, fala: String): String =
        listOf(base.trim(), fala.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    val gravador = remember {
        GravadorVoz(
            context = contexto,
            aoParcial = { texto = juntar(textoBase, it) },
            aoFinal = { texto = juntar(textoBase, it) },
            aoErro = { mensagem = it },
            aoEstado = { estado = it },
            rotulo = "momento",
        )
    }
    DisposableEffect(gravador) {
        onDispose { gravador.destruir() }
    }

    val lifecycleOwner = generateSequence(contexto) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<LifecycleOwner>()
        .firstOrNull()
    DisposableEffect(lifecycleOwner, gravador) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) {
                gravador.cancelar()
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }

    val pedirMicrofone = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { concedida ->
        if (concedida) {
            mensagem = null
            textoBase = texto
            gravador.iniciar()
        } else {
            mensagem = "Permissão de microfone negada. Você ainda pode digitar o momento."
        }
    }

    fun iniciarGravacao() {
        if (!gravador.disponivel) {
            mensagem = "Reconhecimento de voz indisponível neste aparelho. Digite o momento."
            return
        }
        if (estado != EstadoGravacao.OCIOSO) return
        mensagem = null
        textoBase = texto
        val concedida = contexto.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (concedida) {
            gravador.iniciar()
        } else {
            pedirMicrofone.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    AlertDialog(
        onDismissRequest = {
            gravador.cancelar()
            onDismiss()
        },
        title = { Text("Anotar Momento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = {
                        texto = it
                        mensagem = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = estado == EstadoGravacao.OCIOSO,
                    label = { Text("O que aconteceu?") },
                    minLines = 3,
                )
                when (estado) {
                    EstadoGravacao.OUVINDO -> Text(
                        text = "🔴 Ouvindo... Fale o momento",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    EstadoGravacao.PROCESSANDO -> Text(
                        text = "⏳ Processando o áudio...",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    EstadoGravacao.OCIOSO -> Unit
                }
                mensagem?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (estado) {
                        EstadoGravacao.OUVINDO -> Button(onClick = { gravador.parar() }) {
                            Text("Parar")
                        }
                        EstadoGravacao.PROCESSANDO -> Button(onClick = {}, enabled = false) {
                            Text("Processando...")
                        }
                        EstadoGravacao.OCIOSO -> Button(onClick = { iniciarGravacao() }) {
                            Text("🎙️ Ditar")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSalvar(texto.trim()) },
                enabled = texto.isNotBlank() && estado == EstadoGravacao.OCIOSO,
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    gravador.cancelar()
                    onDismiss()
                },
            ) {
                Text("Cancelar")
            }
        },
    )
}
