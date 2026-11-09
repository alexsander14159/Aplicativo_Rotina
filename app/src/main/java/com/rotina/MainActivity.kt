package com.rotina

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.rotina.data.RotinaDb
import com.rotina.notif.obterHorario
import com.rotina.notif.salvarEReagendar
import com.rotina.ui.RotinaViewModel
import com.rotina.ui.RotinaViewModelFactory
import com.rotina.ui.TelaMetas
import com.rotina.ui.TelaMetasEdit
import com.rotina.ui.TelaRegistroTreino
import com.rotina.ui.TelaTreinosEdit
import com.rotina.ui.TelaChatJarvis
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val pedeNotificacao =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* resultado tratado em mostrar() */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = ViewModelProvider(
            this,
            RotinaViewModelFactory(RotinaDb.get(applicationContext).rotinaDao()),
        )[RotinaViewModel::class.java]
        val (horaInicial, minutoInicial) = obterHorario(applicationContext)
        setContent {
            val telaEdicao = rememberSaveable { mutableStateOf(false) }
            val modoTreinosEdit = rememberSaveable { mutableStateOf(false) }
            val modoRegistroTreino = rememberSaveable { mutableStateOf(false) }
            val modoChatJarvis = rememberSaveable { mutableStateOf(false) }
            val horaLembrete = rememberSaveable { mutableStateOf(horaInicial) }
            val minutoLembrete = rememberSaveable { mutableStateOf(minutoInicial) }
            if (modoChatJarvis.value) {
                TelaChatJarvis(
                    vm = vm,
                    onVoltar = { modoChatJarvis.value = false },
                )
            } else if (telaEdicao.value) {
                TelaMetasEdit(
                    vm = vm,
                    onVoltar = { telaEdicao.value = false },
                )
            } else if (modoTreinosEdit.value) {
                TelaTreinosEdit(
                    vm = vm,
                    onVoltar = { modoTreinosEdit.value = false },
                )
            } else if (modoRegistroTreino.value) {
                TelaRegistroTreino(
                    vm = vm,
                    onVoltar = { modoRegistroTreino.value = false },
                )
            } else {
                TelaMetas(
                    vm = vm,
                    lembreteHora = horaLembrete.value,
                    lembreteMinuto = minutoLembrete.value,
                    onAbrirCalendario = {
                        val selecionada = LocalDate.ofEpochDay(vm.dataSelecionada.value)
                        DatePickerDialog(
                            this@MainActivity,
                            { _, ano, mes, dia ->
                                vm.selecionarData(LocalDate.of(ano, mes + 1, dia).toEpochDay())
                            },
                            selecionada.year,
                            selecionada.monthValue - 1,
                            selecionada.dayOfMonth,
                        ).show()
                    },
                    onEditarLembrete = {
                        TimePickerDialog(
                            this@MainActivity,
                            { _, h, m ->
                                horaLembrete.value = h
                                minutoLembrete.value = m
                                salvarEReagendar(applicationContext, h, m)
                            },
                            horaLembrete.value,
                            minutoLembrete.value,
                            true,
                        ).show()
                    },
                    onTesteLembrete = if (BuildConfig.DEBUG) {
                        { com.rotina.notif.mostrar(this) }
                    } else {
                        null
                    },
                    onEditarMetas = { telaEdicao.value = true },
                    onEditarTreinos = { modoTreinosEdit.value = true },
                    onAnotarTreino = { modoRegistroTreino.value = true },
                    onAbrirChat = { modoChatJarvis.value = true },
                )
            }
        }

        // Permissão de notificações (API 33+), pedida na primeira abertura.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedida = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedida) {
                pedeNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
