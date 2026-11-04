# STATUS — Rotina App

Atualizado: 2026-11-03

## Estado atual: Fase 3 — Marco 6 (Jarvis) em andamento; Tarefa 6.1 concluída e validada no celular; Marcos 1–5 concluídos
**Próxima ação:** aguardar o maestro definir a Tarefa 6.2 (motor de contexto); não iniciar sem spec aprovada.

## ✅ Versões fixadas (Tarefa 1)
Gradle 9.6.0 (cache local) | AGP 9.4.0 (**Kotlin embutido — plugin `kotlin-android` NÃO existe mais**) | Kotlin/KGP 2.2.10 | Compose BOM 2026.09.00 | activity-compose 1.13.0 | lifecycle 2.11.0 | Room 2.8.5 | WorkManager 2.12.0 | KSP 2.3.10 | JUnit 4.13.2 / JDK 17 / Build Tools 36 / compileSdk 37

## Agentes
| Terminal | Modelo | Papel |
|---|---|---|
| OpenCode (maestro) | GLM-5.3-Flash Free | Maestro + Dev |
| OpenCode 1 | Nemotron (gratuito) | Ajudante leve: textos, documentação, revisão de leitura |
| OpenCode 2 | DeepSeek V4.1 Flash Free | DEV: implementação e validação no aparelho |

---

## ✅ Ambiente (FASE 0)
- JDK 17.0.12 LTS | Android Studio | Android SDK `C:\Users\Alex\AppData\Local\Android\Sdk`
- adb 1.0.41 | Build Tools 36.0.0 | Platform android-37
- `ANDROID_HOME` definido | `platform-tools` no PATH
- Gradle: usará Gradle Wrapper no projeto

**Dispositivo de teste:** TECNO SPARK GO 1 (código de modelo KL4) / Android 14 (API 34)
`minSdk = 26` | `targetSdk = 37` | `compileSdk = 37`

---

## ✅ MCPs instalados

| Nome | Comando | Status |
|---|---|---|
| context7 | `npx -y @upstash/context7-mcp` | ✅ enabled |
| chrome-devtools | `npx -y chrome-devtools-mcp@latest` | ✅ enabled (web only) |
| mobile-mcp | `npx -y @mobilenext/mobile-mcp@latest` | ✅ enabled |
| droidmind | `uvx droidmind --adb-host localhost --adb-port 5037` | ✅ enabled |

**Descartado:** `zillow/auto-mobile` — arquivado pelo Zillow em Dez/2025, sem manutenção.

---

## ✅ Skills instaladas

| Nome | Origem | Local |
|---|---|---|
| android-cli (plugin) | oficial AGY | `plugins/android-cli-plugin` |
| task-observer | rebelytics | `skills/task-observer` |
| systematic-debugging | AGY | `skills/systematic-debugging` |
| test-driven-development | AGY | `skills/test-driven-development` |
| svg-logo-designer | AGY | `skills/svg-logo-designer` |
| mobile-design | sickn33/antigravity-awesome-skills | `skills/mobile-design` |
| platform-design-android | ehmo/platform-design-skills | `skills/platform-design-android` |

## ✅ Regras de código (projeto)
| Nome | Origem | Local |
|---|---|---|
| compose-rules.md | PatrickJS/awesome-cursorrules | `.gemini/compose-rules.md` |

---

## 🚧 Fase 3 — andamento (Marcos 1–5)

| Tarefa | Estado |
|---|---|
| Marco 2.1 Dados v2 (migration, entities, DAO) | ✅ (`89872d2` — migration provada no banco real) |
| Marco 2.2 Dia do usuário (submetas, alvo, folga) | ✅ (`dcbdc27` — folga ON→OFF provada visual+DB) |
| Marco 2.3 Gerenciar metas (CRUD) + testes | ✅ (`e11ca02` — TDD 4/4; criar/editar/mover/apagar e submetas provados no celular) |
| Marco 2.4 Fechamento Marco 2 | ✅ (seed+schema conferidos; migração, folga, submetas, CRUD e TDD validados; pendência menor: tap no checkbox no modelo novo requer ajuste de foco — documentado) |
| Marco 3.1 Lembrete configurável + notificação clicável | ✅ (TimePicker + SharedPreferences; reagenda; toque na notificação abre o app — verificado no celular) |
| Marco 3.2 Navegação por datas + resumo do dia | ✅ (flatMapLatest; ←/Seguinte/Hoje; card com % e badge Folga — verificado no celular) |
| Marco 3.3 Seletor de data por calendário | ✅ (DatePickerDialog nativo ao tocar na data; abre no dia selecionado — verificado no celular) |
| Marco 4.1 Dados do treino da semana (Room + migration 2→3) | ✅ (`TreinoSemana`, DAO e migration; `user_version=3` e tabela conferidos no celular, 7 metas preservadas) |
| Marco 4.2 Treino do dia + edição semanal | ✅ (card na tela principal + editor dos 7 dias; salvou Terça no celular e o card atualizou na hora; DB confirmou a linha) |
| Marco 4.3 Diário do treino realizado (Room + migration 3→4) | ✅ (`RegistroTreinoDia`/`ExercicioRegistro`, DAO e migration; `user_version=4`, schemas/índice e dados preservados conferidos no celular) |
| Marco 4.4 Interface do diário de treino (registro, exercícios, cardio) | ✅ (TDD 8/8; tela com concluído/observações/cardio/exercícios; card com badge/contagem/cardio; salvar, reabrir, apagar e cold start verificados no celular) |
| Ajuste de UI Rolagem única da tela principal | ✅ (`TelaMetas` com um único `LazyColumn`; cabeçalho, resumo, treino, lembrete e metas rolam juntos — verificado no celular) |
| Correção de vigência temporal de metas/submetas (migration 4→5) | ✅ (`845213d` — `dataInicio`/`dataFim`; meta futura aparece só a partir da data de início; soft delete preserva histórico; 23/23 testes JVM; verificado no celular) |
| Marco 5.1/5.2 Diário por áudio → texto (Room v6 + SpeechRecognizer nativo) | ✅ (`cbc33be` — `DiarioDia` + DAO + migration 5→6; card com gravação, permissão, edição e salvamento; 31/31 testes JVM; `assembleDebug`; transcrição real "meu dia foi produtivo" salva automaticamente; isolamento por data e cold start verificados no celular) |
| Marco 6.1 Histórico da IA + chave da API (Room v7) | ✅ (`d477b0b` — `MensagemIa` + DAO + migration 6→7 e `IaConfig`; 31/31 testes JVM; `assembleDebug`; migração real `user_version=7`/`integrity_check=ok`; dados antigos preservados; `mensagens_ia` vazia) |

**Commits:** `5139192` esqueleto → `05f2d63` Room+seed → `32aff6c` checklist → `1f04113` docs → `eda4fa9` fix upsert/toggleable → `a52dff8` lembrete diario → `cbc33be` diario por audio (marco 5) → `d477b0b` historico da IA e chave (marco 6)

**Rulings (decisões anotadas ao vivo):**
- T1: `kotlin-android` removido — AGP 9 tem **Kotlin embutido** (erro mostrado com texto completo, causa confirmada na doc oficial "Migrate to built-in Kotlin")
- T2: DTO `EstadoMeta` **achatado** (sem `@Embedded`) — evita bug conhecido do KSP/built-in Kotlin (`[MissingType]`, família issue #3053); sem mudança semântica
- T2: `arrayOf<Any>(nome, ordem)` no seed — o aviso do compilador (inferência p/ `Comparable`) quebraria o binding no celular
- T3 (além do plano): `DAO.obter()` + `Mutex` no `alternar` — protege contra toques rápidos duplicados; custo: 1 consulta a mais por toque
- T3 (bug do plano): **`@Upsert` do Room resolve conflito de índice único com UPDATE por PRIMARY KEY** — item chega com `id=0` → `WHERE id=0` → 0 linhas afetadas, silencioso (marcava, mas nunca desmarcava!). Fix: `atual.copy(concluida = ...)` preserva o id real; item de verdade novo usa `id=0` para inserir. Confirmado lendo o SQL gerado (`RotinaDao_Impl.kt`) e o banco via `run-as`+Python
- T3 (UI): linha do checklist usa **`Row.toggleable` + Checkbox passivo (`onCheckedChange = null`)** — um único handler por toque (acessibilidade certa, `Role.Checkbox`)
- T3 (ferramentas): **dump do banco no aparelho** = `python -c ... subprocess adb exec-out run-as cat` (PowerShell corrompe binário via `>`/UTF-16); `uiautomator dump` provou focus/foco do app
- T4 (bug achado no aparelho): manifest sem `android:icon` → `applicationInfo.icon == 0` → `IllegalArgumentException: Invalid notification (no valid small icon)` e **crash** no botão de teste. Fix: `res/drawable/ic_notificacao.xml` (sino) + `android:icon` no manifest + `setSmallIcon(R.drawable.ic_notificacao)`
- T4 (imprevisto): usuário usando o aparelho em paralelo → toque do adb caía no Instagram; solução: am start + conferir `mCurrentFocus` + tocar, em cadeia única
- Geral: verificação no aparelho **agrupada** (celular desconectado na hora) — descoberta de bug de runtime fica deslocada no tempo; mitigado por checklist manual detalhado
- 3.1: horário do lembrete em `SharedPreferences` (`rotina_prefs`); `agendar()` no start usa **KEEP** (não desloca), `salvarEReagendar()` usa **CANCEL_AND_REENQUEUE** (novo horário vale já); notificação usa `PendingIntent` imutável para `MainActivity`. Verificado no celular: 21:30 persistido, job com latência +6h59m (próximo 21:30) e toque na notificação abriu o app; horário restaurado para 20:00
- 3.2: `dataSelecionada: MutableStateFlow<Long>` + `flatMapLatest` para metas/submetas/dia; escritas capturam a data no clique; resumo/rótulo em funções puras (`resumoDoDia` com arredondamento, `rotuloData`) cobertas por 5 testes JVM. Verificado no celular: ←/Seguinte/Hoje, resumo 1/7=14% e 2/7=29%, badge Folga; estado restaurado (0/7, folga off)
- 3.3: data no topo é clicável e abre `DatePickerDialog` nativo iniciando em `dataSelecionada`; callback grava `LocalDate.of(ano, mes+1, dia).toEpochDay()` via `selecionarData()`. Verificado no celular: mês anterior → 15/10/2026, botão "Hoje" → 03/11/2026
- 4.1: entidade `TreinoSemana` com PK natural (`diaSemana` 1–7) e `@Upsert`; migration `MIGRACAO_2_3` cria `treinos_semana` (sem FK, sem seed). Nome do arquivo real é `RotinaDb.kt` (a tarefa citava `RotinaDatabase.kt`). Verificado no celular: `user_version=3`, tabela com schema esperado, `room_master_table` atualizado e 7 metas preservadas
- 4.2: card "Treino do Dia" segue o dia da semana de `dataSelecionada`; editor dos 7 dias com título/descrição e "Salvar" individual. Erro de compilação pego e corrigido: `items(1..7)` caía na sobrecarga `count: Int` → `items((1..7).toList())`. Verificado no celular: salvar Terça atualizou o card na hora; DB com `(2, 'Peito e Triceps', 'Supino, crucifixo e Triceps corda')`; navegar para segunda mostrou "Nenhum treino..." e voltar para hoje restaurou o treino
- 4.3: `RegistroTreinoDia` (PK natural `data`) e `ExercicioRegistro` (PK autoGenerate + índice em `data`); DAO com flows `registroTreinoDoDia`/`exerciciosDoDia`, `@Upsert` e `apagarExercicio(id)`; migration `MIGRACAO_3_4` no companion (padrão das anteriores) e `version=4`. Sem teste JVM novo (schema/DAO gerados pelo Room; validação por compilação + migration real, mesmo padrão da 4.1). Verificado no celular: `user_version=4`, `PRAGMA integrity_check=ok`, schemas exatos, índice `index_exercicios_registro_data`, 7 metas e treino da Terça preservados, `room_master_table` atualizado e app abriu sem crash
- 4.4: diário em `TelaRegistroTreino` (data selecionada, checkboxes de concluído/cardio, observações, lista e diálogo de exercício); helpers puros `resumoExercicio`/`resumoCardio`/`resumoExerciciosAnotados` com TDD 8/8 (19 testes no total). Erro de compilação pego: `registro?.alteracoes` não existe (campo real `alteracoesRotina`) → corrigido. No celular: marcar concluído, observação, "Supino Reto" 4 séries/10 reps/30 kg e cardio 20 min Esteira; card exibiu badge, "Exercícios: 1 anotado" e "Cardio: 20 min (Esteira)"; apagar exercício de teste e cold start confirmados; DB `registro_treino_dia (20760, 1, ..., 1, 'Esteira', 20)` e `exercicios_registro (1, 20760, 'Supino Reto', 4, '10', '30 kg', 1)`. Navegar para 02/11 não mostrou resumo e voltar para hoje restaurou
- Ajuste de UI: `TelaMetas` agora tem um único `LazyColumn` raiz (padding 24, espaçamento 8); cabeçalho, navegação de data, resumo, treino, botões de lembrete e metas/submetas são itens da mesma lista. No celular: swipe para cima removeu o cabeçalho "Rotina" da tela junto com o conteúdo e revelou as metas; swipe para baixo restaurou o topo; 19 testes e `assembleDebug` verdes, sem crash
- 5.0 (vigência): `Meta`/`Submeta` ganharam `dataInicio` (default 0 = desde sempre) e `dataFim` (NULL = sem fim); `MIGRACAO_4_5` adiciona as colunas e `version=5`. No celular: `user_version=5`, `integrity_check=ok`, schema/defaults exatos, 7 metas e 2 submetas originais, 14 checklist, 3 `submeta_dia`, treino/registro preservados. O plano citava `inserirMeta`/`inserirSubmeta` (inexistentes); usei `metaSalvar`/`submetaSalvarCfg` (@Upsert) sob o `Mutex` existente. `moverMeta` passou a considerar só itens vigentes; ao encerrar/apagar não compacto ordens, para não reescrever o histórico (lacunas são inofensivas). Diálogo diferencia "Apagar" (criado na data) de "Encerrar" (histórico preservado). Teste no celular: calendário → 05/11, "Meta Teste 05" + "Sub Teste 05" visíveis em 05/11 e ausentes em 04/11/03/11 (resumo 0/8 vs 0/7); soft delete de meta criada em 01/11 e encerrada em 03/11 sumiu de hoje e permaneceu em 01/11; hard delete limpou o teste temporário. `testDebugUnitTest` 23/23 (4 novos de vigência), `assembleDebug`, cold start sem erros. Dados de teste deixados de propósito: "Meta Teste 05" e "Sub Teste 05" em 05/11
- 5.1/5.2 (diário por áudio): `DiarioDia` (PK `data`), DAO `diarioDoDia`/`salvarDiario` (`@Upsert`), Room v6 + `MIGRACAO_5_6`; `SpeechRecognizer` nativo pt-BR com parciais e estados OCIOSO/OUVINDO/PROCESSANDO; `RECORD_AUDIO` em runtime + `<queries>` para `RecognitionService`; nenhum áudio gravado em arquivo. Card com gravar/parar/editar/salvar; auto-save do resultado final; bloqueio de navegação com edição suja; `ON_STOP` cancela e salva rascunho. TDD 8/8 novos (31/31 total), `assembleDebug`. No celular: migração real `user_version=6`/`integrity_check=ok` e dados preservados; permissão concedida; transcrição real "meu dia foi produtivo" salva automaticamente; edição "ok" salva e o aviso de não salvo bloqueou Anterior; calendário 04/11 vazio e volta a 03/11 restaurou; cold start e logcat sem erros. Desvios: `salvarTextoDiario(texto, data opcional)` + `trim()`; `<queries>`; estado PROCESSANDO. Dados de teste: diário de 03/11 (epoch 20760) "meu dia foi produtivo"
- Revisão final 5.1/5.2: sem itens críticos/importantes remanescentes; corrigida a corrida entre Parar/processando e resultado final e o espelho de data antiga; auto-save de rascunho no `ON_STOP` mantido por decisão de produto
- 6.1: `MensagemIa` (`mensagens_ia`, PK autoGenerate, índices `data`/`criadoEm`), quatro métodos no DAO, Room `version=7` + `MIGRACAO_6_7` e `IaConfig` (SharedPreferences `rotina_ia_prefs`, `MODE_PRIVATE`). Sem nova lógica pura, então sem teste JVM novo; a suíte existente foi a regressão (31/31). No celular: cold start `COLD` sem crash/FATAL, `user_version=7`, `integrity_check=ok`, schema/índices exatos, `mensagens_ia` vazia, `room_master_table` atualizado e hash das linhas antigas idêntico ao snapshot v6. O banco real tem 8 metas (6 seed + 1 manual + `Meta Teste 05`, preservado de propósito), 1 diário de 03/11 e 1 registro de treino; nenhum foi alterado. Ruling: `MIGRACAO_6_7` ficou `private` como as migrações anteriores (mesma semântica); a chave não é exibida nem logada em nenhum ponto e não foi gravada nesta tarefa.

## ✅ Fase 2 — Plano: docs/PLANO.md criado (aguardando sua revisão)
- Marcos 1–6 com critério de pronto; **Marco 1 detalhado** (5 tarefas): metas diárias checklist + Room + lembrete 20:00
- Decisões para confirmar na revisão: (a) lembrete às 20:00 ok? (b) semana de segunda a domingo ok?
- Próximo passo: aprovação → Fase 3 executa o Marco 1 (Tarefa 1: esqueleto do projeto)
