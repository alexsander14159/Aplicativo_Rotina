# TAREFAS — Rotina App

Atualizado: 2026-11-06

## Marco 6 — Tarefa 6.3: Linha do Tempo de Notas Rápidas ("De Pouco em Pouco")

OpenCode #2 (DEV), siga estas instruções passo a passo:

### 1. Entidade `app/src/main/java/com/rotina/data/NotaDia.kt`:
```kotlin
package com.rotina.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notas_dia",
    indices = [Index(value = ["data"]), Index(value = ["criadoEm"])]
)
data class NotaDia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long, // epochDay
    val hora: String, // ex: "09:30"
    val texto: String,
    val criadoEm: Long = System.currentTimeMillis()
)
```

### 2. No `app/src/main/java/com/rotina/data/RotinaDao.kt`, adicionar:
```kotlin
    @Query("SELECT * FROM notas_dia WHERE data = :data ORDER BY criadoEm ASC")
    fun notasDoDia(data: Long): Flow<List<NotaDia>>

    @Upsert
    suspend fun salvarNota(nota: NotaDia)

    @Query("DELETE FROM notas_dia WHERE id = :id")
    suspend fun apagarNota(id: Long)
```

### 3. No `app/src/main/java/com/rotina/data/RotinaDb.kt`:
- Aumentar `version = 8` e incluir `NotaDia::class` nas entities.
- Criar a migração `MIGRACAO_7_8`:
```kotlin
val MIGRACAO_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `notas_dia` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `data` INTEGER NOT NULL,
                `hora` TEXT NOT NULL,
                `texto` TEXT NOT NULL,
                `criadoEm` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notas_dia_data` ON `notas_dia` (`data`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notas_dia_criadoEm` ON `notas_dia` (`criadoEm`)")
    }
}
```
- Registrar `.addMigrations(..., MIGRACAO_7_8)`.

### 4. No `app/src/main/java/com/rotina/ia/ContextoIa.kt`:
- Adicionar `val notasRapidas: List<String>` em `ContextoDia`.
- Na função `coletar(dao, data, perfil)`:
  `val notas = dao.notasDoDia(data).first().map { "${it.hora}: ${it.texto}" }`
- Na função `resumoTexto(c: ContextoDia)`:
  Se `c.notasRapidas.isNotEmpty()`, adicionar:
  `linhas += "Momentos registrados ao longo do dia:"`
  `linhas += c.notasRapidas`
- Atualizar os testes unitários em `ContextoIaTest.kt` para cobrir o campo `notasRapidas`.

### 5. No `app/src/main/java/com/rotina/ui/RotinaViewModel.kt`:
- Conectar reativamente:
```kotlin
    val notasDoDia: StateFlow<List<NotaDia>> = dataSelecionada
        .flatMapLatest { dao.notasDoDia(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun adicionarNota(texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            val horaFormatada = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            dao.salvarNota(
                NotaDia(
                    data = dataSelecionada.value,
                    hora = horaFormatada,
                    texto = texto.trim()
                )
            )
        }
    }

    fun apagarNota(id: Long) {
        viewModelScope.launch {
            dao.apagarNota(id)
        }
    }
```

### 6. Na UI (`app/src/main/java/com/rotina/ui/TelaMetas.kt`):
- No card do Diário / Momentos:
  - Título: **"Momentos & Diário do Dia"**
  - Lista de notas já adicionadas (linha com chip de horário `09:30`, texto e botão de lixeira para apagar).
  - Botão principal: **"+ Anotar Momento"** (abre diálogo com campo de texto e botão de microfone usando `SpeechRecognizer` para ditar por voz ou digitar rápido).
  - Ao salvar no diálogo, chama `vm.adicionarNota(texto)` e limpa o campo.

### 7. Compilar e Testar:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Instale e teste no celular real (TECNO KL4):
- Tocar em "+ Anotar Momento", ditar ou escrever "1ª refeição concluída, 500ml de água" e salvar.
- Conferir que aparece na lista com o horário atual (ex: "07:35").
- Adicionar outro momento: "Estudei Kotlin por 1 hora".
- Mudar de data no calendário: confirmar que os momentos pertencem apenas à data gravada.
- Retornar e confirmar a persistência.

### 8. Commit:
```powershell
git add -A
git commit -m "feat: linha do tempo de notas rapidas do dia de pouco em pouco (tarefa 6.3)"
```

Se houver erro, pare e mostre o erro completo.
