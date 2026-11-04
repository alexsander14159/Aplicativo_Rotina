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

### Tarefa 6.2 — Motor de Contexto (sem UI, sem rede)
**Responsável:** OpenCode #2 (DEV). Room continua **v7** (sem migration). Nenhuma dependência nova. Toda lógica pura roda em JVM (TDD).

#### 1. DAO — adicionar em `RotinaDao.kt` (histórico truncado, leitura pontual):
```kotlin
    @Query("SELECT * FROM mensagens_ia ORDER BY criadoEm DESC LIMIT :limite")
    suspend fun ultimasMensagensIa(limite: Int): List<MensagemIa>
```
Quem consumir converte para ordem ascendente (inverte a lista).

#### 2. `app/src/main/java/com/rotina/ia/PerfilIa.kt` — perfil adaptativo (mesmo prefs `rotina_ia_prefs`, chave `perfil_jarvis`):
```kotlin
package com.rotina.ia

import android.content.Context

/**
 * Texto que descreve quem o usuário é na visão do Jarvis: estilo, dificuldades,
 * o que funciona para ele. Começa com a persona padrão e é EVOLUÍDO pela própria IA
 * (a escrita pela IA chega na 6.3; aqui só há leitura e salvar).
 * Nunca registre o conteúdo da chave em logs; o perfil não é segredo, a chave sim.
 */
object PerfilIa {
    private const val KEY_PERFIL = "perfil_jarvis"

    const val PERFIL_PADRAO: String = "Você é o Jarvis, mentor de rotina em pt-BR: direto, motivador e honesto. " +
        "Comemora vitórias reais, aponta falhas sem enrolação e dá um conselho claro para o dia seguinte. " +
        "Conheça o dia do usuário pelos dados abaixo antes de responder."

    fun obter(context: Context): String =
        context.getSharedPreferences("rotina_ia_prefs", Context.MODE_PRIVATE)
            .getString(KEY_PERFIL, "")?.trim().orEmpty().ifEmpty { PERFIL_PADRAO }

    fun salvar(context: Context, texto: String) {
        context.getSharedPreferences("rotina_ia_prefs", Context.MODE_PRIVATE)
            .edit().putString(KEY_PERFIL, texto.trim()).apply()
    }
}
```

#### 3. `app/src/main/java/com/rotina/ia/ContextoIa.kt` — coleta o dia (suspend; use `Dispatchers.IO` no chamador):
```kotlin
data class ContextoDia(
    val data: Long, // epochDay
    val folga: Boolean,
    val metas: List<String>,       // "- Meta (alvo): Feita" | "Pendente"
    val submetas: List<String>,    // igual, por submeta
    val treinoPlanejado: String?,  // titulo+descricao de treinos_semana do diaSemana
    val treinoRealizado: Boolean?,
    val exercicios: List<String>,  // "Supino Reto: 4 série(s) x 10 reps @ 30 kg"
    val cardio: String?,           // "20 min Esteira"
    val observacoes: String?,
    val diarioTexto: String,       // diário de voz do dia (pode ser vazia)
    val perfil: String,            // PerfilIa.obter(...)
)
```
- `fun coletar(dao: RotinaDao, data: Long, perfil: String): ContextoDia` (suspend): usa `checklistDoDia(data).first()`, `submetasDoDia(data).first()`, `diaObter(data)`, `diarioDoDia(data).first()`, `treinoDoDia(diaSemana).first()` (diaSemana = `LocalDate.ofEpochDay(data).dayOfWeek.value`, 1=seg ... 7=dom), `registroTreinoDoDia(data).first()` e `exerciciosDoDia(data).first()`.
- `fun resumoTexto(c: ContextoDia): String` — puro e determinístico, uma linha por campo; dia vazio ⇒ "Nenhum registro no dia." + metas pendentes; folga ⇒ "Hoje é folga".

#### 4. `app/src/main/java/com/rotina/ia/PromptJarvis.kt` — puro:
```kotlin
data class MensagemPrompt(val papel: String, val texto: String) // "system" | "user" | "model"

fun historicoParaEnvio(mensagens: List<MensagemIa>, limite: Int = 30): List<MensagemIa>
// mantém as `limite` mais recentes, em ordem ASC; entrada pode estar em qualquer ordem

fun montar(contexto: ContextoDia, historico: List<MensagemIa>, pergunta: String?): List<MensagemPrompt>
// system = PerfilIa.PERFIL_PADRAO (se perfil == padrão) OU persona base + perfil salvo + resumoTexto(contexto)
// depois: histórico (origem "USER"→"user"; "ASSISTANT"→"model")
// depois: se pergunta != null → MensagemPrompt("user", pergunta); se null → mensagem user pedindo o briefing do dia
```

#### 5. TDD (JVM, `app/src/test/java/com/rotina/ia/`):
- `historicoParaEnvio`: >30 mantém as 30 mais recentes em ordem ASC; <30 devolve tudo; >devolve vazio com lista vazia.
- `resumoTexto`: dia completo marca feito/pendente, treino+cardio+observações; dia vazio; folga.
- `montar`: system contém perfil e resumo; mapping USER/ASSISTANT correto; `pergunta=null` cria o pedido de briefing.

#### 6. Gradle (1 tarefa por vez): `testDebugUnitTest` → `assembleDebug` → `install -r` + cold start (sem mudança de UI; confirmar que nada quebra e dados preservados).

#### 7. Commit:
- `feat: motor de contexto e prompt do Jarvis com perfil adaptativo e historico truncado (tarefa 6.2)`
- depois docs: registra em `docs/STATUS.md` rulings + resultado dos testes/celular.

### Tarefa 6.3 (aguardando 6.2) — Interface do Chat e Relatório Diário
- Botão na tela principal abrir a tela de chat ("Conversar com o Jarvis").
- Botão de briefing do dia: relatório diário em tom de mentor (analisa metas, treino com cargas/cardio e diário de voz).
- Tela de Configurações da IA: digitar a chave do Gemini 1x (guardada pelo IaConfig; nunca exibida — só status "chave definida").
- Privacidade: mostrar o que será enviado antes do primeiro envio do dia.

