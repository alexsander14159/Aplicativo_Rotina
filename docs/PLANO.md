# PLANO — App Rotina (v2: rotina diária customizável)

> **Execução (Fase 3):** maestro/dev + OpenCode 1 para tarefas leves (via maestri). 1 tarefa Gradle por vez. Commits pt-BR pequenos. Antes de "pronto": compilar + instalar + testar no celular.

**Objetivo:** app nativo de **rotina diária** — metas customizáveis com submetas, alvo do dia (ex.: "2h"), lembrete com horário configurável que abre o resumo do dia, treino editável, diário por áudio com IA no fim.

**Arquitetura:** MVVM — Room (fonte de verdade) + ViewModel StateFlow + Compose M3 + WorkManager. 1 módulo `app`.

**Decisões da spec v2 (aprovadas em 30/10/2026):**
- **Sem nada semanal**: a âncora única é a data do celular (epochDay, como no Marco 1)
- Metas podem ser **renomeadas/criadas/apagadas/reordenadas** — as 6 do seed são apenas o início
- Meta pode ter **submetas** (ex.: "Horas de estudo" → "Estudar Java"), cada submeta se marca no dia
- **Meta é independente**: checkbox próprio; submetas não fecham a meta
- **Alvo diário** em texto na meta ("2h", "8 copos"); valor digitado entra depois, se faltar
- **Lembrete configurável no app** (horário) e notificação leva ao **resumo do dia** (Marco 3)
- **"Hoje é folga"**: um toque marca o dia atípico (feriado etc.) — histórico não cobra

## Esquema Room (v2, fixado)
```kotlin
@Entity(tableName = "metas_dia")
data class Meta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val ordem: Int,
    val alvoTexto: String = ""           // "2h de estudo", "" = sem alvo
)

@Entity(tableName = "submetas",            // item da rotina, ligado a uma meta
        indices = [Index("metaId")])
data class Submeta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metaId: Long,
    val nome: String,
    val ordem: Int
)

@Entity(tableName = "submeta_dia",         // marcação diária da submeta
        indices = [Index(value = ["data", "submetaId"], unique = true)])
data class SubmetaDia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long,
    val submetaId: Long,
    val concluida: Boolean
)

@Entity(tableName = "dias")                // anotação do dia: folga
data class Dia(
    @PrimaryKey val data: Long,          // chave natural — sem autoGenerate
    val folga: Boolean
)

// checklist (Marco 1) segue igual: ChecklistDia(data, metaId, concluida)
```
- **Sem FK/CASCADE**: apagar meta = `@Transaction` apagando submeta_dia das submetas dela, depois submetas, depois checklist, depois a meta (leções do Marco 1 — ver Rulings em STATUS.md)

## Restrições globais (valem para toda tarefa)
- minSdk 26 | targetSdk 37 | compileSdk 37 — aparelho: TECNO SPARK GO 1 (KL4), Android 14
- **1 tarefa Gradle por vez**; só `assembleDebug`, `testDebugUnitTest` e `install -r`
- Versões fixadas em docs/STATUS.md; **nunca re-add `kotlin-android`** (AGP 9 embutido); kapt morto — KSP
- API keys nunca no código; `.gitignore` cobre local.properties
- Commits pt-BR (`feat:`, `fix:`, `docs:`); migrations Room com version bump explícito
- **Ruling upsert-PK**: escrita que atualiza linha existente preserva o `id` real (`copy()`); exceção: `Dia`, cuja PK é natural (`data` — `@Upsert` resolve pela própria chave)
- Deleção `@Transaction` conforme bloco acima (sem FK)

## Review Focus (falhas prováveis que nenhum teste cobre)
| Entrada/falha | Comportamento esperado | Teste pinado |
|---|---|---|
| App instalado v1 atualiza para v2 | Migration preserva metas + histórico; `alvoTexto` vazio; nada quebra | Tarefa 2.1 |
| Apagar meta com submetas e marcas | `@Transaction` mata dependências; tela do dia re-renderiza limpa, sem órfãs | 2.3 |
| Toques rapidíssimos em submetas | Mesmo padrão Mutex + `copy(id)` do Marco 1 — sem flip duplo | 2.2 |
| Usuário apagar TODAS as metas | Tela vazia com botão "Nova meta"; sem crash | 2.3 |
| "Hoje é folga" ligado/desligado no dia | Anotação por data; checklist continua editável normalmente | 2.2 |

---

## Marcos seguintes (critérios de pronto)
- **Marco 3 — Lembrete configurável + resumo do dia:** horário editável no app (reagenda com KEEP corretamente); tocar na notificação abre o app no dia; tela "resumo" mostra dias passados (marcados, folgas) navegáveis. *Pronto:* horário muda e a notificação segue; histórico testado no celular.
- **Marco 4 — Treino da semana editável:** treinos por dia da semana (CRUD simples, visual na rotina). *Pronto:* CRUD persiste e aparece; testado no celular.
- **Marco 5 — Diário por áudio → texto:** ANTES de codificar: pesquisa STT local vs API (custo/privacidade), entrego pro usuário; confirmar o que o plano Gemini estudante cobre. *Pronto:* gravar → texto salvo por dia.
- **Marco 6 — IA analisa os dias:** resumo de registros + diários. *Pronto:* análise aparece no app; chave fora do código.

---

## ✅ Marco 1 — concluído (verificado no celular)
Checklist diário das 6 metas com Room, seed, lembrete fixo 20:00 com botão de teste em debug. Commits: `5139192` → `05f2d63` → `32aff6c` → `1f04113` → `eda4fa9` → `a52dff8` → `d70b315` → `e96cf5d`. Detalhes e rulings em docs/STATUS.md.

---

## Marco 2 — Metas/submetas customizáveis + alvo do dia + folga (em execução)

### Tarefa 1: Dados v2 — migration + entities + DAO
**Files:** Modify `data/Meta.kt` (coluna `alvoTexto`), `data/RotinaDb.kt` (version 2 + MIGRATION + seed ajustado se preciso); Create `data/Submeta.kt`, `data/SubmetaDia.kt`, `data/Dia.kt`; Modify `data/RotinaDao.kt` (novas funções + DTOs achatados, sem `@Embedded` — Ruling T2).
**Interfaces (produz, assinaturas fixas):**
- DAO: `metas(): Flow<List<Meta>>` (mantém) | `checklistDoDia(data: Long): Flow<List<EstadoMeta>>` (SQL atualizado com `alvoTexto`) | `obter(data, metaId)` / `salvar(ChecklistDia)` (mantém) | `submetasDoDia(data): Flow<List<EstadoSubmeta>>` | `submetaObter(data, submetaId): SubmetaDia?` | `submetaSalvar(SubmetaDia)` `@Upsert` | `metaSalvar(Meta)` `@Upsert` | `metaApagar(Meta)` `@Delete` | `submetaSalvarCfg(Submeta)` `@Upsert` | `submetaApagarCfg(Submeta)` `@Delete` | `diaCarregar(data): Flow<Dia?>` | `diaSalvar(Dia)` `@Upsert` | `apagarMetaComDependencias(meta)` `@Transaction`
- DTOs flat: `EstadoMeta(id, nome, ordem, alvoTexto, concluida: Boolean?)`; `EstadoSubmeta(id, metaId, nome, ordem, metaOrdem, concluida: Boolean?)`
**Migration (v1→v2):**
```sql
ALTER TABLE metas_dia ADD COLUMN alvoTexto TEXT NOT NULL DEFAULT '';
CREATE TABLE IF NOT EXISTS submetas (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, metaId INTEGER NOT NULL, nome TEXT NOT NULL, ordem INTEGER NOT NULL);
CREATE INDEX IF NOT EXISTS index_submetas_metaId ON submetas (metaId);
CREATE TABLE IF NOT EXISTS submeta_dia (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, data INTEGER NOT NULL, submetaId INTEGER NOT NULL, concluida INTEGER NOT NULL, PRIMARY KEY(id));
CREATE UNIQUE INDEX IF NOT EXISTS index_submeta_dia_data_submetaId ON submeta_dia (data, submetaId);
CREATE TABLE IF NOT EXISTS dias (data INTEGER NOT NULL, folga INTEGER NOT NULL, PRIMARY KEY(data));
```
- [x] Passo 1: entities + DTOs + DAO (SQLs conforme assinaturas; JOINs achatados, ordenação `m.ordem, s.ordem` — além do plano: `diaObter(data)` suspend para leitura consistente sob Mutex)
- [x] Passo 2: RotinaDb version 2 + MIGRATION_1_2 (SQL acima; `alvoTexto` com `@ColumnInfo(defaultValue = "")` para casar schema novo e migrado — seed não muda) ✅
- [x] Passo 3: `gradlew assembleDebug` OK
- [x] Passo 4: celular — `install -r` (mantém DB!) → abrir sem crash → confirmado por `run-as`+Python: coluna `alvoTexto` ok, tabelas novas ok, metas/histórico intactos (migration real no banco vivo)
- [x] Passo 5: commit `feat: dados v2 com submetas, alvo do dia e folga` → `89872d2`

### Tarefa 2: Dia do usuário atualizado — alvo + submetas + folga
**Files:** Modify `ui/RotinaViewModel.kt`, `ui/TelaMetas.kt`, `MainActivity.kt` (nada se UI declinável); Create nada.
**Interfaces:** Consumes Tarefa 1; Produces no VM: `submetasDoDia: StateFlow<List<EstadoSubmeta>>`, `dia: StateFlow<Dia?>`, `alternarSubmeta(submetaId: Long)`, `alternarFolga()`.
- [x] Passo 1: VM — flow `dia` + `alternarFolga` (upsert `Dia(data, folga)`) + `alternarSubmeta` (Mutex + obter/copy/salvar, padrão Ruling upsert-PK) ✅ `dcbdc27`
- [x] Passo 2: UI — submetas listadas recuadas sob a meta (poucas linhas), toggle por submeta; alvo (`alvoTexto`) como texto de apoio; chip "Hoje é folga" no topo ✅
- [x] Passo 3: `assembleDebug` → instalar → testar no celular: erro de padding duplo na compilação (mostrado, corrigido); folga ON→OFF provada no visual + DB (`dias[20756]=1→0`); `submeta_dia` sem órfãs. Toggle de submeta fica pro e2e da Tarefa 3 (sem submetas criáveis ainda)
- [x] Passo 4: commit `feat: submetas marcáveis, alvo exibido e folga do dia` → `dcbdc27`

### Tarefa 3: Gerenciar metas (CRUD) + submetas CRUD
**Files:** Create `ui/TelaMetasEdit.kt` (ou ficheiro similar); Modify VM, TelaMetas, MainActivity.
**Interfaces:** Consumes Tarefa 1; Produces no VM: `criarMeta(nome: String, alvoTexto: String)`, `atualizarMeta(meta: Meta)`, `apagarMeta(metaId: Long)` (chama `apagarMetaComDependencias` e reordena), `criarSubmeta(metaId: Long, nome: String)`, `apagarSubmeta(submeta: Submeta)`, `moverMeta(metaId: Long, deslocamento: Int)`.
- [ ] Passo 1: TDD lógica pura (JVM): `ordensAposTrocar(List<Int>, de, para)` (swap) e `ordensAposRemover(List<Int>, pos)` (compacta) — RED → GREEN
- [ ] Passo 2: tela "Metas" (acessível pelo topo): lista com editar/apagar/mover; diálogo de editar (nome + alvo) e submetas (adicionar/apagar), confirmação no apagar meta
- [ ] Passo 3: `gradlew testDebugUnitTest` OK (2 novas) → `assembleDebug`
- [ ] Passo 4: celular — criar meta, renomear, trocar de ordem, apagar meta com submetas (Review Focus), verificar DB limpo sem órfãs via `run-as`
- [ ] Passo 5: commit `feat: gerenciar metas e submetas`

### Tarefa 4: Fechamento Marco 2
- [ ] Passo 1: desinstalar + reinstalar limpo → seed 6 metas + tudo criável do zero (Review Focus primeira execução)
- [ ] Passo 2: checklist manual completo (marcar/desmarcar, submetas, folga, CRUD, histórico do dia anterior estático?)
- [ ] Passo 3: STATUS.md atualizado → pedir aprovação do Marco 3

**Pronto quando:** metas/submetas/alvos/folga funcionam e persistem no celular; migration v1→v2 suave; commits pt-BR; docs atualizados.
