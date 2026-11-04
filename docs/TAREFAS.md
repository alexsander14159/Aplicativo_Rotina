# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Marco 5 — Diário por áudio → texto (Etapa 5.1 e 5.2)

OpenCode #2 (DEV), siga estas instruções passo a passo:

### 1. Modelagem no Room (`app/src/main/java/com/rotina/data/DiarioDia.kt`):
```kotlin
package com.rotina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diario_dia")
data class DiarioDia(
    @PrimaryKey val data: Long,
    val texto: String = "",
    val atualizadoEm: Long = System.currentTimeMillis()
)
```

### 2. No `app/src/main/java/com/rotina/data/RotinaDao.kt`, adicionar:
```kotlin
    @Query("SELECT * FROM diario_dia WHERE data = :data")
    fun diarioDoDia(data: Long): Flow<DiarioDia?>

    @Upsert
    suspend fun salvarDiario(diario: DiarioDia)
```

### 3. No `app/src/main/java/com/rotina/data/RotinaDb.kt`:
- Aumentar `version = 6` e adicionar `DiarioDia::class` nas entities.
- Criar a migração `MIGRACAO_5_6`:
```kotlin
val MIGRACAO_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `diario_dia` (
                `data` INTEGER NOT NULL,
                `texto` TEXT NOT NULL,
                `atualizadoEm` INTEGER NOT NULL,
                PRIMARY KEY(`data`)
            )
            """.trimIndent()
        )
    }
}
```
- Registrar `.addMigrations(..., MIGRACAO_5_6)` no construtor do banco.

---

### 4. Permissão no `app/src/main/AndroidManifest.xml`:
Adicionar no manifest (fora da tag application):
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

---

### 5. No `app/src/main/java/com/rotina/ui/RotinaViewModel.kt`:
- Conectar o diário reativo à `dataSelecionada`:
```kotlin
    val diarioDoDia: StateFlow<DiarioDia?> = dataSelecionada
        .flatMapLatest { dao.diarioDoDia(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    fun salvarTextoDiario(texto: String) {
        viewModelScope.launch {
            dao.salvarDiario(
                DiarioDia(
                    data = dataSelecionada.value,
                    texto = texto,
                    atualizadoEm = System.currentTimeMillis()
                )
            )
        }
    }
```

---

### 6. Transcrição Nativa de Áudio e Interface:
Crie `app/src/main/java/com/rotina/audio/GravadorVoz.kt` (ou gerencie na Activity/Composable):
- Usar `android.speech.SpeechRecognizer.createSpeechRecognizer(context)`.
- Intent:
  ```kotlin
  Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
  }
  ```
- Listener `RecognitionListener` que atualiza o texto transcrito em `onResults` e `onPartialResults`.

Na tela principal (`TelaMetas.kt`):
- Adicionar o Card **"Diário do Dia"**:
  - Exibe o texto transcrito salvo naquele dia.
  - Botão **"🎙️ Gravar Diário"** (com solicitação da permissão `RECORD_AUDIO` em tempo de execução).
  - Enquanto estiver gravando/ouvindo, exibe indicador visual: "🔴 Ouvindo... Fale o seu diário" e botão "Parar".
  - Opção de editar o texto digitando caso queira corrigir algo.

---

### 7. Compilar e Testar:
Execute uma tarefa por vez:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Instale e teste no celular TECNO KL4:
- Verificar permissão de microfone.
- Gravar uma frase de teste ou salvar texto no diário.
- Mudar de dia no calendário e verificar que o diário fica salvo no dia correto.

### 8. Commit:
```powershell
git add -A
git commit -m "feat: diario por audio gravado e transcrito no aparelho com Room v6 (marco 5)"
```

Se houver erro, pare e mostre o erro completo.
