# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Marco 6 — Assistente Inteligente ("Jarvis") de Rotina

### Tarefa 6.1 (Concluída e validada no celular — commit d477b0b)
- **Objetivo:** Persistência do histórico de conversas com a IA no Room (v6 → v7) + Armazenamento seguro de Chave de API.
- **Responsável:** OpenCode #2 (DEV)

#### 1. Entidade `app/src/main/java/com/rotina/data/MensagemIa.kt`:
```kotlin
package com.rotina.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mensagens_ia",
    indices = [Index(value = ["data"]), Index(value = ["criadoEm"])]
)
data class MensagemIa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long, // epochDay do dia em que a conversa ocorreu
    val origem: String, // "USER" ou "ASSISTANT"
    val conteudo: String,
    val criadoEm: Long = System.currentTimeMillis()
)
```

#### 2. Adicionar ao `RotinaDao.kt`:
```kotlin
    @Query("SELECT * FROM mensagens_ia ORDER BY criadoEm ASC")
    fun todasMensagensIa(): Flow<List<MensagemIa>>

    @Query("SELECT * FROM mensagens_ia WHERE data = :data ORDER BY criadoEm ASC")
    fun mensagensIaDoDia(data: Long): Flow<List<MensagemIa>>

    @Upsert
    suspend fun salvarMensagemIa(msg: MensagemIa)

    @Query("DELETE FROM mensagens_ia")
    suspend fun limparHistoricoIa()
```

#### 3. No `RotinaDb.kt`:
- Aumentar `version = 7` e adicionar `MensagemIa::class` nas entities.
- Criar a migração `MIGRACAO_6_7`:
```kotlin
val MIGRACAO_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `mensagens_ia` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `data` INTEGER NOT NULL,
                `origem` TEXT NOT NULL,
                `conteudo` TEXT NOT NULL,
                `criadoEm` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mensagens_ia_data` ON `mensagens_ia` (`data`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mensagens_ia_criadoEm` ON `mensagens_ia` (`criadoEm`)")
    }
}
```
- Registrar `.addMigrations(..., MIGRACAO_6_7)`.

#### 4. Gerenciador de Chave de API (`app/src/main/java/com/rotina/ia/IaConfig.kt`):
- Funções para salvar e obter a chave do Gemini em `SharedPreferences` de forma privada no aparelho:
```kotlin
package com.rotina.ia

import android.content.Context

object IaConfig {
    private const val PREFS = "rotina_ia_prefs"
    private const val KEY_GEMINI = "gemini_api_key"

    fun obterChave(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_GEMINI, "") ?: ""
    }

    fun salvarChave(context: Context, chave: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GEMINI, chave.trim())
            .apply()
    }
}
```

#### 5. Compilar e Testar:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Validar no celular TECNO KL4.

#### 6. Commit:
```powershell
git add -A
git commit -m "feat: persistencia de historico de mensagens da IA no Room v7 e gerenciador de chave (tarefa 6.1)"
```

---

### Tarefa 6.2 (aguardando 6.1) — Motor de Contexto
- Função que reúne todo o contexto do dia para enviar à IA: metas/submetas, folga, treino da semana + registro do treino, diário de áudio do dia e histórico do chat (`mensagens_ia`, truncado para as mensagens mais recentes).
- Prompt do sistema do "Jarvis" (tom de mentor: comemora vitórias, aponta falhas, aconselha) + perfil adaptativo salvo no app, atualizado pela própria IA com o que aprende do usuário.
- Detalhamento fino (modelo Gemini, truncamento, custo) será escrito e aprovado antes de delegar.

### Tarefa 6.3 (aguardando 6.2) — Interface do Chat e Relatório Diário
- Botão na tela principal abrir a tela de chat ("Conversar com o Jarvis").
- Botão de briefing do dia: relatório diário em tom de mentor (analisa metas, treino com cargas/cardio e diário de voz).
- Tela de Configurações da IA: digitar a chave do Gemini 1x (guardada pelo IaConfig; nunca exibida — só status "chave definida").
- Privacidade: mostrar o que será enviado antes do primeiro envio do dia.

