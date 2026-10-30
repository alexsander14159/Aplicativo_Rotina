package com.rotina

import android.app.Application
import com.rotina.notif.agendar

/** Application do app Rotina: agenda o lembrete diário ao subir (sobrevive a reinícios). */
class RotinaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        agendar(this)
    }
}
