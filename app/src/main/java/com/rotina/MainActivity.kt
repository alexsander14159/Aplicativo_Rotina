package com.rotina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.rotina.data.RotinaDb
import com.rotina.ui.RotinaViewModel
import com.rotina.ui.RotinaViewModelFactory
import com.rotina.ui.TelaMetas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = ViewModelProvider(
            this,
            RotinaViewModelFactory(RotinaDb.get(applicationContext).rotinaDao()),
        )[RotinaViewModel::class.java]
        setContent {
            TelaMetas(vm)
        }
    }
}
