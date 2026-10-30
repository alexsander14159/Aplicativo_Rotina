# PLANO — App Rotina

> **Execução (Fase 3):** implementação pelo maestro/dev, ajuda leve do OpenCode 1 (textos/docs), máx. 2 agentes. Passos com `- [ ]` para acompanhar progresso.

**Objetivo:** app Android nativo de produtividade pessoal — metas diárias/semanais, treino da semana editável, notificações e diário por áudio com IA (fase posterior).

**Arquitetura:** MVVM simples — Room como fonte de verdade, ViewModel com StateFlow, UI em Jetpack Compose (Material 3), WorkManager para lembretes. **1 módulo único (`app`)** para manter Gradle barato no notebook fraco.

**Stack confirmada:** Kotlin, Jetpack Compose (Material 3), Room, WorkManager.

## Restrições globais (valem para toda tarefa)
- minSdk 26 | targetSdk 37 | compileSdk 37 — aparelho de teste: TECNO SPARK GO 1, Android 14
- **1 tarefa Gradle por vez**; usar apenas `assembleDebug`, `installDebug` e testes unitários pontuais; nunca encadear builds
- Versões das dependências fixadas na Tarefa 1 (via context7) e registradas em docs/STATUS.md
- Chaves de API: nunca no código — ambiente ou `local.properties`; `.gitignore` desde a Tarefa 1
- Commits pequenos em português (`feat:`, `fix:`, `docs:`)
- Nada de instalação de ferramentas novas sem aprovação; OpenCode 1 só tarefas leves

## Critério de pronto (todas as tarefas)
`gradlew assembleDebug` OK → `adb install -r` no celular → funcional testado no aparelho → commit pt-BR → STATUS.md atualizado.

## Review Focus (falhas prováveis que nenhum teste cobre)
| Entrada/falha | Comportamento esperado | Teste pinado |
|---|---|---|
| Primeira execução, banco vazio | As 6 metas aparecem (seed) | Tarefa 2 |
| Dia muda no aparelho | Checklist novo e limpo para a data de hoje | Tarefa 3 |
| Usuário nega POST_NOTIFICATIONS | App não trava; lembrete só não é exibido | Tarefa 4 |
| Processo morto durante uso | Estado vem do Room ao reabrir, sem duplicar | Tarefa 3 |
| Reinício do aparelho | Lembrete re-agenda sozinho (KEEP no Application) | Tarefa 4 |

---

## Marco 1 — Metas diárias com checklist + Room + lembrete (próximo)

**Meta do marco:** as 6 metas diárias do spec aparecerem como checklist marcável, salvo em Room, com lembrete diário às **20:00**, rodando no seu celular.

**Esquema Room (fixado):**
```kotlin
@Entity(tableName = "metas_dia")
data class Meta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,      // "Horas de estudo", "Água", "Questões feitas", "Academia", "Dias produtivos", "Comer direito"
    val ordem: Int
)

@Entity(tableName = "checklist",
        indices = [Index(value = ["data", "metaId"], unique = true)])
data class ChecklistDia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long,        // LocalDate.toEpochDay() do aparelho
    val metaId: Long,
    val concluida: Boolean
)

data class EstadoMeta(val meta: Meta, val concluida: Boolean?)  // null = ainda sem registro hoje

@Dao
interface RotinaDao {
    fun metas(): Flow<List<Meta>>
    fun checklistDoDia(data: Long): Flow<List<EstadoMeta>>
    @Upsert suspend fun salvar(item: ChecklistDia)
}
```

### Tarefa 1: Esqueleto do projeto
**Files:** Create `settings.gradle.kts`, `build.gradle.kts` (raiz + `app`), `gradle/libs.versions.toml`, `app/src/main/AndroidManifest.xml`, `MainActivity.kt` (título "Rotina", tema M3), `.gitignore` (`local.properties`, `.gradle/`, `build/`, `*.jks`, `.idea/`).
**Interfaces:** Produces applicationId `com.rotina`.
- [ ] Passo 1: fixar versões em `libs.versions.toml` (Compose BOM, Room, WorkManager, lifecycle) consultando context7 e registrar em docs/STATUS.md
- [ ] Passo 2: MainActivity com tela mínima em Material 3
- [ ] Passo 3: `gradlew assembleDebug` → OK
- [ ] Passo 4: `adb install -r` → "Rotina" abre no celular
- [ ] Passo 5: `git init` + commit `feat: esqueleto do projeto com tema Material 3`

### Tarefa 2: Room + seed
**Files:** Create `data/Meta.kt`, `data/ChecklistDia.kt`, `data/RotinaDao.kt`, `data/RotinaDb.kt` (seed das 6 metas via `RoomDatabase.Callback`).
**Interfaces:** Consumes Restrições globais; Produces `RotinaDao` e `RotinaDb.get(context)` (assinaturas acima).
- [ ] Passo 1: entidades, DAO e banco + seed ordem 1..6
- [ ] Passo 2: `assembleDebug` OK → instalar → primeiro run no celular mostra as 6 metas (Review Focus: seed)
- [ ] Passo 3: commit `feat: room com metas e checklist diário`

### Tarefa 3: ViewModel + tela de checklist
**Files:** Create `ui/RotinaViewModel.kt`, `ui/TelaMetas.kt`; Modify `MainActivity.kt`.
**Interfaces:** Consumes `RotinaDao`; Produces `RotinaViewModel` (`val metas: StateFlow<List<EstadoMeta>>`, `fun alternar(metaId: Long)`) e composable `TelaMetas(vm)`.
- [ ] Passo 1: teste JVM da lógica pura `proximoEstado(atual: Boolean?): Boolean` — null→true, false→true, true→false. Run: `gradlew testDebugUnitTest` (1 tarefa só). Esperado: PASS
- [ ] Passo 2: ViewModel + LazyColumn com checkbox e data de hoje no topo
- [ ] Passo 3: no celular — marcar 2 metas, fechar app, reabrir → seguem marcadas; mudar a data do aparelho → lista nova vazia
- [ ] Passo 4: commit `feat: checklist diário persistente`

### Tarefa 4: Lembrete diário (WorkManager + NotificationManager)
**Files:** Create `notif/Lembrete.kt` (`mostrar(context)`, `agendar(context)`), `RotinaApp.kt` (Application); Modify Manifest (`POST_NOTIFICATIONS`), MainActivity (permissão runtime, canal "lembretes").
**Interfaces:** Consumes Tarefa 3 (tela); Produces canal "lembretes" + agendamento único `lembrete-diario` (PeriodicWork 24h, initialDelay até 20:00, política KEEP).
- [ ] Passo 1: canal + permissão runtime pedida na primeira tela; negação não trava o app
- [ ] Passo 2: agendar no `onCreate` do Application com KEEP
- [ ] Passo 3: botão temporário **apenas em debug** "Testar lembrete" → notificação aparece no celular
- [ ] Passo 4: commit `feat: lembrete diário às 20:00`

### Tarefa 5: Fechamento do Marco 1
- [ ] Passo 1: desinstalar, reinstalar do zero → seed + checklist OK (Review Focus)
- [ ] Passo 2: checklist manual completo no aparelho (itens do marco)
- [ ] Passo 3: STATUS.md atualizado → pedir aprovação do Marco 2

**Pronto quando:** instala e roda no TECNO; 6 metas aparecem; marcação persiste; notificação de teste aparece; lembrete agendado às 20:00; commits pt-BR.

---

## Marcos seguintes (resumo + critério de pronto)

**Marco 2 — Metas semanais + valores:** alvos semanais editáveis (horas de estudo, água, questões, academia, comer direito) e "Dias produtivos" como métrica da semana (conta dias com tudo marcado); semana de **segunda a domingo**.
*Pronto:* tela semanal mostra alvo + progresso correto; testado no celular.

**Marco 3 — Treino da semana editável:** criar/editar/remover treinos por dia da semana.
*Pronto:* CRUD de treinos persiste e aparece na semana; testado no celular.

**Marco 4 — Notificações configuráveis:** horário do lembrete editável + avisos por regra (ex.: "até 18h sem marcar nada").
*Pronto:* horário editável no app; aviso por regra chega; testado no celular.

**Marco 5 — Diário por áudio → texto:** antes de codificar, pesquisa de opções (STT local do Android vs API), custo e privacidade, entregue para você; confirmar o que o plano Gemini estudante cobre.
*Pronto:* gravar áudio → texto salvo por dia; opção aprovada previamente; testado no celular.

**Marco 6 — IA analisa os dias:** análise da IA sobre registros + diários.
*Pronto:* resumo gerado pela IA; chaves fora do código; testado no celular.

## Regras de execução (Fase 3)
- Máx. 2 agentes; OpenCode 1 apenas tarefas leves (maestri; fallback: docs/TAREFAS.md + STATUS.md)
- TDD onde é barato (lógica pura em JVM); o resto verificado no aparelho
- Antes de dizer "pronto": compilar, instalar e testar no celular
- Errar = mostrar o erro completo e parar
