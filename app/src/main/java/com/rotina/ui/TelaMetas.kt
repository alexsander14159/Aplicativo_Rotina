package com.rotina.ui

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
    onAbrirChat: (() -> Unit)? = null,
) {
    val metas by vm.metas.collectAsState()
    val submetas by vm.submetas.collectAsState()
    val dia by vm.dia.collectAsState()
    val treino by vm.treinoDoDia.collectAsState()
    val registroTreino by vm.registroTreinoDoDia.collectAsState()
    val exerciciosRegistro by vm.exerciciosDoDia.collectAsState()
    val diario by vm.diarioDoDia.collectAsState()
    val notas by vm.notasDoDia.collectAsState()
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
    var mostrarDialogoMomento by rememberSaveable { mutableStateOf(false) }

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rotina",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Seu dia em foco e disciplina",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilterChip(
                        selected = folga,
                        onClick = { vm.alternarFolga() },
                        label = { Text(if (ehHoje) "Hoje é folga" else "Folga") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (onEditarMetas != null) {
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onEditarMetas,
                            enabled = !diarioPendente,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Metas", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Navegação por data
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = vm::irParaDiaAnterior,
                            enabled = !diarioPendente
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Dia Anterior",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    enabled = onAbrirCalendario != null && !diarioPendente,
                                ) {
                                    onAbrirCalendario?.invoke()
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = rotuloData(dataSelecionada, hoje),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = vm::irParaDiaSeguinte,
                            enabled = !diarioPendente
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Dia Seguinte",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (!ehHoje) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = vm::irParaHoje,
                            enabled = !diarioPendente,
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text("Voltar para Hoje", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Resumo do dia com barra de progresso visual
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Resumo do Dia",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (folga) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp),
                                ) {
                                    Text(
                                        text = "Folga",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                        val progresso = if (metas.isNotEmpty()) concluidas.toFloat() / metas.size.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progresso },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            strokeCap = StrokeCap.Round
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(progresso * 100).toInt()}% concluído",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$concluidas de ${metas.size} metas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Acesso ao Jarvis
            if (onAbrirChat != null) {
                item {
                    Surface(
                        onClick = onAbrirChat,
                        enabled = !diarioPendente,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Jarvis (Mentor Inteligente)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Toque para conversar ou pedir o relatório do dia",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("💬", style = MaterialTheme.typography.titleMedium)
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
                        notas = notas,
                        onApagarNota = vm::apagarNota,
                        onAnotarMomento = { mostrarDialogoMomento = true },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Treino do Dia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = treino?.titulo?.takeIf { it.isNotBlank() }
                                ?: "Nenhum treino definido para hoje",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val descricao = treino?.descricao.orEmpty()
                        if (descricao.isNotBlank()) {
                            Text(
                                text = descricao,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (registroTreino?.concluido == true) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = "✅ Treino Concluído",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                        resumoExerciciosAnotados(exerciciosRegistro.size)?.let { resumo ->
                            Text(text = "• $resumo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        resumoCardio(
                            fezCardio = registroTreino?.fezCardio == true,
                            tipo = registroTreino?.tipoCardio.orEmpty(),
                            minutos = registroTreino?.cardioMinutos ?: 0,
                        )?.let { resumo ->
                            Text(text = "• $resumo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onAnotarTreino != null) {
                                Button(
                                    onClick = onAnotarTreino,
                                    enabled = !diarioPendente,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Anotar Cargas")
                                }
                            }
                            if (onEditarTreinos != null) {
                                TextButton(
                                    onClick = onEditarTreinos,
                                    enabled = !diarioPendente,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Editar Semana")
                                }
                            }
                        }
                    }
                }
            }

            if (onEditarLembrete != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = onEditarLembrete,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Lembrete diário: ${LocalTime.of(lembreteHora, lembreteMinuto)}")
                        }
                    }
                }
            }
            if (onTesteLembrete != null) {
                item {
                    Button(
                        onClick = onTesteLembrete,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Testar lembrete (debug)")
                    }
                }
            }

            items(metas, key = { it.id }) { meta ->
                val concluida = meta.concluida == true
                val infoIcone = remember(meta.nome) { obterIconeMeta(meta.nome) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = concluida,
                            role = Role.Checkbox,
                            onValueChange = { vm.alternar(meta.id) },
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (concluida) {
                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(infoIcone.containerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = infoIcone.icon,
                                    contentDescription = null,
                                    tint = infoIcone.contentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = meta.nome,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (concluida) FontWeight.Normal else FontWeight.SemiBold,
                                    color = if (concluida) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    textDecoration = if (concluida) TextDecoration.LineThrough else null
                                )
                                if (meta.alvoTexto.isNotEmpty()) {
                                    Text(
                                        text = "Alvo: ${meta.alvoTexto}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Checkbox(
                                checked = concluida,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }

                        val submetas = submetasPorMeta[meta.id].orEmpty()
                        if (submetas.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            submetas.forEach { sub ->
                                val subConcluida = sub.concluida == true
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .toggleable(
                                            value = subConcluida,
                                            role = Role.Checkbox,
                                            onValueChange = { vm.alternarSubmeta(sub.id) },
                                        )
                                        .padding(start = 36.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = subConcluida,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.secondary,
                                            checkmarkColor = MaterialTheme.colorScheme.onSecondary
                                        )
                                    )
                                    Text(
                                        text = sub.nome,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (subConcluida) {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        textDecoration = if (subConcluida) TextDecoration.LineThrough else null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (mostrarDialogoMomento) {
            AnotarMomentoDialog(
                onDismiss = { mostrarDialogoMomento = false },
                onSalvar = { texto ->
                    vm.adicionarNota(texto)
                    mostrarDialogoMomento = false
                },
            )
        }
    }
}
