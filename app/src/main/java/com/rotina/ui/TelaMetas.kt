package com.rotina.ui

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.rotina.audio.EstadoGravacao
import com.rotina.audio.GravadorVoz
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
    onEditarTreinos: (() -> Unit)? = null,
    onAnotarTreino: (() -> Unit)? = null,
) {
    val metas by vm.metas.collectAsState()
    val submetas by vm.submetas.collectAsState()
    val dia by vm.dia.collectAsState()
    val treino by vm.treinoDoDia.collectAsState()
    val registroTreino by vm.registroTreinoDoDia.collectAsState()
    val exerciciosRegistro by vm.exerciciosDoDia.collectAsState()
    val diario by vm.diarioDoDia.collectAsState()
    val dataSelecionada by vm.dataSelecionada.collectAsState()
    val hoje = LocalDate.now().toEpochDay()
    val ehHoje = dataSelecionada == hoje
    val folga = dia?.folga == true
    val submetasPorMeta = remember(submetas) { submetas.groupBy { it.metaId } }
    val concluidas = metas.count { it.concluida == true }

    val contexto = LocalContext.current
    var textoDiario by rememberSaveable(dataSelecionada) { mutableStateOf("") }
    var textoEditado by rememberSaveable(dataSelecionada) { mutableStateOf(false) }
    var mensagemDiario by rememberSaveable(dataSelecionada) { mutableStateOf<String?>(null) }
    var estadoDiario by remember(dataSelecionada) { mutableStateOf(EstadoGravacao.OCIOSO) }

    val gravadorDiario = remember(dataSelecionada) {
        GravadorVoz(
            context = contexto,
            aoParcial = {
                textoDiario = it
                textoEditado = true
            },
            aoFinal = { texto ->
                textoDiario = texto
                textoEditado = false
                vm.salvarTextoDiario(texto, dataSelecionada)
                mensagemDiario = "Diário transcrito e salvo."
            },
            aoErro = { mensagemDiario = it },
            aoEstado = { estadoDiario = it },
        )
    }
    DisposableEffect(gravadorDiario) {
        onDispose { gravadorDiario.destruir() }
    }
    LaunchedEffect(dataSelecionada, diario?.data, diario?.texto) {
        val salvo = diario
        if (salvo != null &&
            salvo.data == dataSelecionada &&
            (!textoEditado || salvo.texto == textoDiario)
        ) {
            textoDiario = salvo.texto
            textoEditado = false
        }
    }
    val pedirMicrofone = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { concedida ->
        if (concedida) {
            mensagemDiario = null
            gravadorDiario.iniciar()
        } else {
            mensagemDiario = "Permissão de microfone negada. Você ainda pode digitar o diário."
        }
    }
    fun iniciarGravacaoDiario() {
        if (!gravadorDiario.disponivel) {
            mensagemDiario = "Reconhecimento de voz indisponível neste aparelho. Digite o diário."
            return
        }
        if (estadoDiario != EstadoGravacao.OCIOSO) return
        mensagemDiario = null
        val concedida = contexto.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (concedida) {
            gravadorDiario.iniciar()
        } else {
            pedirMicrofone.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val textoAtual by rememberUpdatedState(textoDiario)
    val editadoAtual by rememberUpdatedState(textoEditado)
    val dataAtual by rememberUpdatedState(dataSelecionada)
    val lifecycleOwner = generateSequence(contexto) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<LifecycleOwner>()
        .firstOrNull()
    DisposableEffect(lifecycleOwner, gravadorDiario) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) {
                gravadorDiario.cancelar()
                if (editadoAtual && textoAtual.isNotBlank()) {
                    vm.salvarTextoDiario(textoAtual, dataAtual)
                    textoEditado = false
                }
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }
    val diarioOcupado = estadoDiario != EstadoGravacao.OCIOSO
    val diarioPendente = diarioOcupado || textoEditado

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
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
                            TextButton(onClick = onEditarMetas, enabled = !diarioPendente) {
                                Text("Metas")
                            }
                        }
                    }
                }

                // Navegação por data
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = vm::irParaDiaAnterior,
                            enabled = !diarioPendente,
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
                                .clickable(
                                    enabled = onAbrirCalendario != null && !diarioPendente,
                                ) {
                                    onAbrirCalendario?.invoke()
                                }
                                .padding(vertical = 8.dp),
                        )
                        TextButton(
                            onClick = vm::irParaDiaSeguinte,
                            enabled = !diarioPendente,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text("Seguinte →")
                        }
                    }
                }
                if (!ehHoje) {
                    item {
                        TextButton(onClick = vm::irParaHoje, enabled = !diarioPendente) {
                            Text("Hoje")
                        }
                    }
                }

                // Resumo do dia
                item {
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
                }

                // Diário do dia (texto transcrito/editado)
                item {
                    DiarioCard(
                        texto = textoDiario,
                        estado = estadoDiario,
                        mensagem = mensagemDiario,
                        disponivel = gravadorDiario.disponivel,
                        pendente = textoEditado,
                        temDiarioSalvo = diario?.texto?.isNotBlank() == true,
                        onTextoChange = {
                            textoDiario = it
                            textoEditado = true
                            mensagemDiario = null
                        },
                        onGravar = { iniciarGravacaoDiario() },
                        onParar = { gravadorDiario.parar() },
                        onSalvar = {
                            if (estadoDiario == EstadoGravacao.OCIOSO) {
                                vm.salvarTextoDiario(textoDiario, dataSelecionada)
                                textoEditado = false
                                mensagemDiario = "Diário salvo."
                            }
                        },
                    )
                }

                // Treino do dia (segue o dia da semana da data selecionada)
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Treino do Dia",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = treino?.titulo?.takeIf { it.isNotBlank() }
                                    ?: "Nenhum treino definido para hoje",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            val descricao = treino?.descricao.orEmpty()
                            if (descricao.isNotBlank()) {
                                Text(
                                    text = descricao,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            if (registroTreino?.concluido == true) {
                                Text(
                                    text = "✅ Treino Concluído",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            resumoExerciciosAnotados(exerciciosRegistro.size)?.let { resumo ->
                                Text(text = resumo, style = MaterialTheme.typography.bodySmall)
                            }
                            resumoCardio(
                                fezCardio = registroTreino?.fezCardio == true,
                                tipo = registroTreino?.tipoCardio.orEmpty(),
                                minutos = registroTreino?.cardioMinutos ?: 0,
                            )?.let { resumo ->
                                Text(text = resumo, style = MaterialTheme.typography.bodySmall)
                            }
                            if (onAnotarTreino != null) {
                                Button(onClick = onAnotarTreino, enabled = !diarioPendente) {
                                    Text("Anotar Treino / Cargas")
                                }
                            }
                            if (onEditarTreinos != null) {
                                TextButton(onClick = onEditarTreinos, enabled = !diarioPendente) {
                                    Text("Editar Treinos da Semana")
                                }
                            }
                        }
                    }
                }

                if (onEditarLembrete != null) {
                    item {
                        TextButton(onClick = onEditarLembrete) {
                            Text("Lembrete: ${LocalTime.of(lembreteHora, lembreteMinuto)}")
                        }
                    }
                }
                if (onTesteLembrete != null) {
                    item {
                        Button(onClick = onTesteLembrete) {
                            Text("Testar lembrete (debug)")
                        }
                    }
                }

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
