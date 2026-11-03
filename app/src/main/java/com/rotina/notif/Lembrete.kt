package com.rotina.notif

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rotina.MainActivity
import com.rotina.R
import java.time.Duration
import java.time.LocalDateTime

private const val CANAL = "lembretes"
private const val NOME_TRABALHO = "lembrete-diario"
private const val PREFS = "rotina_prefs"
private const val CHAVE_HORA = "lembrete_hora"
private const val CHAVE_MINUTO = "lembrete_minuto"
private const val HORA_PADRAO = 20
private const val MINUTO_PADRAO = 0

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

    // Toque na notificação abre o app na tela do dia.
    val abrirApp = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        abrirApp,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notificacao = NotificationCompat.Builder(context, CANAL)
        .setSmallIcon(R.drawable.ic_notificacao)
        .setContentTitle("Rotina")
        .setContentText("Hora de registrar as metas de hoje!")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()
    nm.notify(1, notificacao)
}

/** Horário configurado do lembrete, em (hora, minuto). Padrão: 20:00. */
fun obterHorario(context: Context): Pair<Int, Int> {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return prefs.getInt(CHAVE_HORA, HORA_PADRAO) to prefs.getInt(CHAVE_MINUTO, MINUTO_PADRAO)
}

/** Salva o novo horário e reagenda o trabalho diário imediatamente. */
fun salvarEReagendar(context: Context, hora: Int, minuto: Int) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putInt(CHAVE_HORA, hora)
        .putInt(CHAVE_MINUTO, minuto)
        .apply()
    // CANCEL_AND_REENQUEUE garante que o novo horário valha já na próxima execução.
    agendarInterno(context, hora, minuto, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE)
}

/** Agenda o lembrete diário no próximo horário salvo (padrão: 20:00). */
fun agendar(
    context: Context,
    hora: Int = obterHorario(context).first,
    minuto: Int = obterHorario(context).second,
) {
    // KEEP: reiniciar o app não re-agenda nem desloca o horário.
    agendarInterno(context, hora, minuto, ExistingPeriodicWorkPolicy.KEEP)
}

private fun agendarInterno(
    context: Context,
    hora: Int,
    minuto: Int,
    politica: ExistingPeriodicWorkPolicy,
) {
    val agora = LocalDateTime.now()
    val alvo = agora.toLocalDate().atTime(hora, minuto).let {
        if (it.isAfter(agora)) it else it.plusDays(1)
    }
    val atrasoInicial = Duration.between(agora, alvo)
    val pedido = PeriodicWorkRequestBuilder<LembreteWorker>(Duration.ofDays(1))
        .setInitialDelay(atrasoInicial)
        .build()
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(NOME_TRABALHO, politica, pedido)
}
