# STATUS — Rotina App

Atualizado: 2026-10-30

## Estado atual: Fase 3 em execução — Marco 2 (spec v2 "rotina diária" aprovada; Marco 1 concluído no celular)
**Próxima ação:** Tarefa 2.1 — entidades/migration v2 + DAO. EM seguida 2.2 e 2.3.

## ✅ Versões fixadas (Tarefa 1)
Gradle 9.6.0 (cache local) | AGP 9.4.0 (**Kotlin embutido — plugin `kotlin-android` NÃO existe mais**) | Kotlin/KGP 2.2.10 | Compose BOM 2026.09.00 | activity-compose 1.13.0 | lifecycle 2.11.0 | Room 2.8.5 | WorkManager 2.12.0 | KSP 2.3.10 | JUnit 4.13.2 / JDK 17 / Build Tools 36 / compileSdk 37

## Agentes
| Terminal | Modelo | Papel |
|---|---|---|
| OpenCode (maestro) | GLM-5.3-Flash Free | Maestro + Dev |
| OpenCode 1 | Nemotron (gratuito) | Ajudante leve: textos, documentação, revisão de leitura |

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

## 🚧 Fase 3 — andamento (Marco 1)

| Tarefa | Estado |
|---|---|
| Marco 2.1 Dados v2 (migration, entities, DAO) | ✅ (`89872d2` — migration provada no banco real) |
| Marco 2.2 Dia do usuário (submetas, alvo, folga) | ✅ (`dcbdc27` — folga ON→OFF provada visual+DB) |
| Marco 2.3 Gerenciar metas (CRUD) + testes | ⏳ em execução |
| Marco 2.4 Fechamento Marco 2 | ⏳ |

**Commits:** `5139192` esqueleto → `05f2d63` Room+seed → `32aff6c` checklist → `1f04113` docs → `eda4fa9` fix upsert/toggleable → `a52dff8` lembrete diario

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

## ✅ Fase 2 — Plano: docs/PLANO.md criado (aguardando sua revisão)
- Marcos 1–6 com critério de pronto; **Marco 1 detalhado** (5 tarefas): metas diárias checklist + Room + lembrete 20:00
- Decisões para confirmar na revisão: (a) lembrete às 20:00 ok? (b) semana de segunda a domingo ok?
- Próximo passo: aprovação → Fase 3 executa o Marco 1 (Tarefa 1: esqueleto do projeto)
