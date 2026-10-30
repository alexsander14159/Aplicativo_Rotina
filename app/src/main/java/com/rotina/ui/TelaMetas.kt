package com.rotina.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun TelaMetas(vm: RotinaViewModel) {
    val metas by vm.metas.collectAsState()
    val rotuloData = LocalDate.ofEpochDay(vm.dataDeHoje).toString()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = "Rotina", style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = "Metas de hoje — $rotuloData",
                    style = MaterialTheme.typography.titleMedium,
                )
                LazyColumn {
                    items(metas) { estado ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.alternar(estado.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = estado.concluida == true,
                                onCheckedChange = { vm.alternar(estado.id) },
                            )
                            Text(text = estado.nome, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
