package com.rotina.notif

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rotina.R
import java.time.Duration
import java.time.LocalDateTime

private const val CANAL = "lembretes"
private const val NOME_TRABALHO = "lembrete-diario"

/**
 * Trabalho diário do WorkManager: publica o lembrete "hora das metas".
 * Sem permissão de notificação, não publica e encerra com sucesso.
 */
class LembreteWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        mostrar(applicationContext)
        return Result.success()
    }
}

private fun canalCriar(context: Context) {
    val canal = NotificationChannel(CANAL, "Lembretes", NotificationManager.IMPORTANCE_DEFAULT)
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nm.createNotificationChannel(canal)
}

/** Publica a notificação de lembrete (se a permissão estiver concedida). */
fun mostrar(context: Context) {
    val concedida = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    if (!concedida) return
    canalCriar(context)
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificacao = NotificationCompat.Builder(context, CANAL)
        .setSmallIcon(R.drawable.ic_notificacao)
        .setContentTitle("Rotina")
        .setContentText("Hora de registrar as metas de hoje!")
        .setAutoCancel(true)
        .build()
    nm.notify(1, notificacao)
}

/** Agenda o lembrete diário no próximo horário informado (padrão: 20:00). */
fun agendar(context: Context, hora: Int = 20, minuto: Int = 0) {
    val agora = LocalDateTime.now()
    var alvo = agora.toLocalDate().atTime(hora, minuto).let {
        if (it.isAfter(agora)) it else it.plusDays(1)
    }
    val atrasoInicial = Duration.between(agora, alvo)
    val pedido = PeriodicWorkRequestBuilder<LembreteWorker>(Duration.ofDays(1))
        .setInitialDelay(atrasoInicial)
        .build()
    // KEEP: reiniciar o app não re-agenda nem desloca o horário.
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(NOME_TRABALHO, ExistingPeriodicWorkPolicy.KEEP, pedido)
}
