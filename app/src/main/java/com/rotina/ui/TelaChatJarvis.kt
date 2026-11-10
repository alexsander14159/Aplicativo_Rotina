package com.rotina.ui

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.rotina.audio.EstadoGravacao
import com.rotina.audio.GravadorVoz
import com.rotina.data.MensagemIa
import com.rotina.ia.IaConfig
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Chat estilo mensageiro com o Jarvis, incluindo relatório do dia e configuração da chave. */
@Composable
fun TelaChatJarvis(
    vm: RotinaViewModel,
    onVoltar: () -> Unit,
) {
    val contexto = LocalContext.current
    val mensagens by vm.mensagensChat.collectAsState()
    val carregando by vm.carregandoIa.collectAsState()
    val erro by vm.erroIa.collectAsState()

    var texto by rememberSaveable { mutableStateOf("") }
    var mostrarDialogoChave by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    var avisoVoz by rememberSaveable { mutableStateOf<String?>(null) }
    var estadoVoz by remember { mutableStateOf(EstadoGravacao.OCIOSO) }
    var textoBase by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    BackHandler(onBack = onVoltar)

    fun juntar(base: String, fala: String): String =
        listOf(base.trim(), fala.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    val gravador = remember {
        GravadorVoz(
            context = contexto,
            aoParcial = { texto = juntar(textoBase, it) },
            aoFinal = { texto = juntar(textoBase, it) },
            aoErro = { avisoVoz = it },
            aoEstado = { estadoVoz = it },
            rotulo = "texto",
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
            avisoVoz = null
            textoBase = texto
            gravador.iniciar()
        } else {
            avisoVoz = "Permissão de microfone negada. Você ainda pode digitar a mensagem."
        }
    }

    fun iniciarGravacao() {
        if (!gravador.disponivel) {
            avisoVoz = "Reconhecimento de voz indisponível neste aparelho. Digite a mensagem."
            return
        }
        if (estadoVoz != EstadoGravacao.OCIOSO) return
        avisoVoz = null
        textoBase = texto
        val concedida = contexto.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (concedida) {
            gravador.iniciar()
        } else {
            pedirMicrofone.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun enviar() {
        val mensagem = texto.trim()
        if (mensagem.isEmpty() || carregando || estadoVoz != EstadoGravacao.OCIOSO) return
        vm.enviarMensagemJarvis(contexto, mensagem)
        texto = ""
        aviso = null
        avisoVoz = null
    }

    val totalItens = mensagens.size + if (carregando) 1 else 0
    LaunchedEffect(totalItens) {
        if (totalItens > 0) {
            listState.animateScrollToItem(totalItens - 1)
        }
    }

    fun agendarRelatorio() {
        aviso = null
        vm.pedirRelatorioDoDia(contexto)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            BarraSuperiorJarvis(
                carregando = carregando,
                onVoltar = onVoltar,
                onRelatorio = ::agendarRelatorio,
                onChave = { mostrarDialogoChave = true },
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (mensagens.isEmpty() && !carregando) {
                    item(key = "boas-vindas") {
                        Text(
                            text = "Converse com o Jarvis sobre seu dia, metas e treinos. " +
                                "Ele usa o contexto do dia selecionado e suas últimas mensagens.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                items(mensagens, key = { it.id }) { mensagem ->
                    BalaoMensagem(mensagem)
                }

                if (carregando) {
                    item(key = "jarvis-pensando") {
                        BalaoPensando()
                    }
                }
            }

            erro?.let { mensagemErro ->
                AvisoErro(
                    mensagem = mensagemErro,
                    onDismiss = vm::limparErroIa,
                )
            }
            avisoVoz?.let { avisoMicrofone ->
                Text(
                    text = avisoMicrofone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            aviso?.let { mensagemAviso ->
                Text(
                    text = mensagemAviso,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            BarraEnvioJarvis(
                texto = texto,
                estadoVoz = estadoVoz,
                carregando = carregando,
                onTextoChange = {
                    texto = it
                    aviso = null
                    avisoVoz = null
                },
                onIniciarGravacao = ::iniciarGravacao,
                onPararGravacao = { gravador.parar() },
                onEnviar = ::enviar,
            )
        }

        if (mostrarDialogoChave) {
            DialogoChaveGemini(
                temChave = IaConfig.obterChave(contexto).isNotBlank(),
                onDismiss = { mostrarDialogoChave = false },
                onSalvar = { chave ->
                    IaConfig.salvarChave(contexto, chave)
                    mostrarDialogoChave = false
                    aviso = "Chave salva neste aparelho."
                },
                onRemover = {
                    IaConfig.salvarChave(contexto, "")
                    mostrarDialogoChave = false
                    aviso = "Chave removida deste aparelho."
                },
            )
        }
    }
}

@Composable
private fun BarraSuperiorJarvis(
    carregando: Boolean,
    onVoltar: () -> Unit,
    onRelatorio: () -> Unit,
    onChave: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Jarvis (Mentor)",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onChave) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = "Configurar chave do Gemini",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onRelatorio,
                    enabled = !carregando,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Relatório do Dia")
                }
            }
        }
    }
}

@Composable
private fun BalaoMensagem(mensagem: MensagemIa) {
    val doUsuario = mensagem.origem == "USER"
    val cor = if (doUsuario) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val corTexto = if (doUsuario) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shape = if (doUsuario) {
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp,
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 4.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp,
        )
    }
    val alinhamento = if (doUsuario) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alinhamento,
    ) {
        Surface(
            color = cor,
            shape = shape,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                if (doUsuario) {
                    Text(
                        text = mensagem.conteudo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = corTexto,
                    )
                } else {
                    MarkdownText(
                        text = mensagem.conteudo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = corTexto,
                    )
                }
                Text(
                    text = formatarHora(mensagem.criadoEm),
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun BalaoPensando() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp,
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
                Text(
                    text = "Jarvis está pensando...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AvisoErro(
    mensagem: String,
    onDismiss: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mensagem,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    }
}

@Composable
private fun BarraEnvioJarvis(
    texto: String,
    estadoVoz: EstadoGravacao,
    carregando: Boolean,
    onTextoChange: (String) -> Unit,
    onIniciarGravacao: () -> Unit,
    onPararGravacao: () -> Unit,
    onEnviar: () -> Unit,
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Pergunte ao Jarvis...") },
                maxLines = 4,
                enabled = estadoVoz == EstadoGravacao.OCIOSO,
            )
            when (estadoVoz) {
                EstadoGravacao.OUVINDO -> Button(onClick = onPararGravacao) {
                    Text("Parar")
                }
                EstadoGravacao.PROCESSANDO -> Button(onClick = {}, enabled = false) {
                    Text("...")
                }
                EstadoGravacao.OCIOSO -> Button(onClick = onIniciarGravacao) {
                    Text("🎙️")
                }
            }
            Button(
                onClick = onEnviar,
                enabled = texto.isNotBlank() &&
                    !carregando &&
                    estadoVoz == EstadoGravacao.OCIOSO,
            ) {
                Text("Enviar")
            }
        }
    }
}

@Composable
private fun DialogoChaveGemini(
    temChave: Boolean,
    onDismiss: () -> Unit,
    onSalvar: (String) -> Unit,
    onRemover: () -> Unit,
) {
    var chave by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chave do Gemini") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "A chave fica somente neste aparelho. Ao enviar mensagens ou pedir o " +
                        "relatório, o contexto do dia e o histórico recente são enviados ao " +
                        "Google Gemini.",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (temChave) {
                    Text(
                        "Já existe uma chave configurada. Cole uma nova para substituir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedTextField(
                    value = chave,
                    onValueChange = { chave = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chave da API") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSalvar(chave.trim()) },
                enabled = chave.isNotBlank(),
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            Row {
                if (temChave) {
                    TextButton(onClick = onRemover) {
                        Text("Remover")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        },
    )
}

private fun formatarHora(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(FORMATO_HORA)
