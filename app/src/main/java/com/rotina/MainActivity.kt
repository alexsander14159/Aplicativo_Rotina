package com.rotina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rotina.data.RotinaDb

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TelaRotina()
        }
    }
}

@Composable
fun TelaRotina() {
    val context = LocalContext.current
    val dao = remember(context) { RotinaDb.get(context).rotinaDao() }
    val metas by dao.metas().collectAsState(initial = emptyList())
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Rotina", style = MaterialTheme.typography.headlineLarge)
                Text(text = "Metas diárias", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(metas) { meta ->
                        Text(text = meta.nome, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
