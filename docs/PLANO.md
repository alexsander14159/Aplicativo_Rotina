# PLANO — App Rotina (v2: rotina diária customizável)

> **Execução (Fase 3):** maestro/dev + OpenCode 1 para tarefas leves (via maestri). 1 tarefa Gradle por vez. Commits pt-BR pequenos. Antes de "pronto": compilar + instalar + testar no celular.

**Objetivo:** app nativo de **rotina diária** — metas customizáveis com submetas, alvo do dia, lembrete com horário configurável que abre o resumo do dia, treino editável, diário por áudio com IA no fim.

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
    /** Alvo do dia em texto (ex.: "2h de estudo"). "" = sem alvo. */
    @ColumnInfo(defaultValue = "") val alvoTexto: String = "",
)

@Entity(
    tableName = "submetas",
    indices = [Index("metaId")],
)
data class Submeta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metaId: Long,
    val nome: String,
    val ordem: Int,
)

@Entity(tableName = "submeta_dia",
    indices = [Index(value = ["data", "submetaId"], unique = true)])
data class SubmetaDia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** LocalDate.toEpochDay() do aparelho. */
    val data: Long,
    val submetaId: Long,
    val concluida: Boolean,
)

@Entity(tableName = "dias")
data class Dia(
    @PrimaryKey val data: Long,
    val folga: Boolean,
)

// checklist (Marco 1) segue igual: ChecklistDia(data, metaId, concluida)
```

## Restrições globais (valem para toda tarefa)
- minSdk 26 | targetSdk 37 | compileSdk 37 — aparelho: TECNO SPARK GO 1 (KL4), Android 14
- **1 tarefa Gradle por vez**; usar apenas `assembleDebug`, `testDebugUnitTest` e `install -r`; nunca encadear builds
- Versões das dependências fixadas em docs/STATUS.md e registradas lá; **nunca re-add `kotlin-android`** (AGP 9 embutido); kapt morto — KSP
- Chaves de API: nunca no código — ambiente ou `local.properties`; `.gitignore` desde a Tarefa 1
- Commits pequenos em português (`feat:`, `fix:`, `docs:`)
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

## Marcos seguintes (critérios de pronto)
- **Marco 3 — Lembrete configurável + resumo do dia:** horário editável no app (reagenda com KEEP corretamente); tocar na notificação abre o app no dia; tela "resumo" mostra dias passados (marcados, folgas) navegáveis. *Pronto:* horário muda e a notificação segue; histórico testado no celular.
- **Marco 4 — Treino da semana editável:** treinos por dia da semana (CRUD simples, visual na rotina). *Pronto:* CRUD persiste e aparece; testado no celular.
- **Marco 5 — Diário por áudio → texto:** ANTES de codificar, pesquisa de opções (STT local do Android vs API), custo e privacidade, entregue para você; confirmar o que o plano Gemini estudante cobre. *Pronto:* gravar áudio → texto salvo por dia; opção aprovada previamente; testado no celular.
- **Marco 6 — IA analisa os dias:** análise da IA sobre registros + diários. *Pronto:* resumo gerado pela IA; chaves fora do código; testado no celular.

## ✅ Marco 1 — concluído (verificado no celular)
Checklist diário das 6 metas com Room, seed, lembrete fixo 20:00 com botão de teste em debug. Commits: `5139192` → `05f2d63` → `32aff6c` → `1f04113` → `eda4fa9` → `a52dff8` → `d70b315` → `e96cf5d`. Detalhes e rulings em docs/STATUS.md.

## Marco 2 — concluído (rotina diária customizável)
- Dados v2 (entities, submetas, alvo do dia, folga) ✅
- Migration v1→v2 no celular ✅
- Submetas marcáveis, alvo do dia, folga do dia ✅
- Gerenciar metas (CRUD) com TDD 4/4 ✅
- Próximo passo: Marco 3 — Lembrete configurável + resumo do dia

## Marco 6 — Assistente IA ("Jarvis") — plano aprovado (03/11/2026)

**Visão (usuário):** IA tipo Jarvis que guarda todas as mensagens, constrói personalidade com base no que o usuário fala, gera sempre um relatório do dia analisado, e é chamada por um botão para conversar com o próprio contexto do dia (perguntar o que devo melhorar).

**Pilares:**
1. **Memória contínua e personalidade adaptativa** — tabela `mensagens_ia` no Room guarda todas as conversas; a IA se lembra de conversas passadas e dos diários de áudio, aprendendo estilo/dificuldades. Mecanismos: perfil adaptativo salvo no app (texto que a própria IA atualiza) + histórico truncado no envio (custo).
2. **Relatório diário automatizado** — analisa o dia selecionado (metas/submetas/folga, treino: cargas, séries, cardio, diário de voz) em tom de mentor: comemora vitórias, aponta falhas e dá o conselho para o dia seguinte.
3. **Chat interativo ("Conversar com o Jarvis")** — botão na tela principal; tela de chat; a IA já entra sabendo tudo do dia (sem explicar do zero).
4. **Chave de API — segurança** — tela de Configurações da IA (chega na 6.3): chave do Gemini digitada 1x, guardada em `SharedPreferences` privado no aparelho; nunca no código, no Git ou em logs (só status "chave definida").

**Tarefas:** 6.1 banco de dados da IA + guarda da chave (detalhada em docs/TAREFAS.md) → 6.2 motor de contexto (reúne metas/treinos/diários + prompt do Jarvis; perfil adaptativo e truncamento) → 6.3 interface do chat + relatório diário + tela de configurações da chave + aviso de privacidade do envio.

**Riscos acompanhados:** custo/quota da API (truncamento de contexto e limite por dia); privacidade (diário e treinos saem do aparelho para o Gemini — aviso antes do envio); chave de API (MODE_PRIVATE; backup do aparelho pode copiar — nunca logar).

## Regras de execução (Fase 3)
- Máx. 2 agentes; OpenCode 1 apenas tarefas leves (maestri; fallback: docs/TAREFAS.md + STATUS.md)
- TDD onde é barato (lógica pura em JVM); o resto verificado no aparelho
- Antes de dizer "pronto": compilar, instalar e testar no celular
- Errar = mostrar o erro completo e parar