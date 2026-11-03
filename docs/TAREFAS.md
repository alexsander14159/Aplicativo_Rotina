# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Marco 3 — Lembrete configurável + Resumo do dia

### OpenCode #2 (DEV)
- **Tarefa 3.1:** Horário configurável do lembrete + toque na notificação abre o app.
- **Subtarefas:**
  1. `Lembrete.kt`: adicionar `PendingIntent` na notificação direcionando para `MainActivity` (`FLAG_IMMUTABLE`).
  2. Persistência de horário em `SharedPreferences` ("rotina_prefs", chaves `lembrete_hora` padrão 20 e `lembrete_minuto` padrão 0).
  3. Reagendamento via `ExistingPeriodicWorkPolicy.UPDATE` ao alterar o horário.
  4. Componente/diálogo na UI para selecionar o horário do lembrete.
  5. Validar com `.\gradlew.bat testDebugUnitTest` e `.\gradlew.bat assembleDebug`.
- **Status:** Em execução.

---

### OpenCode (Auxiliar/Docs)
- **Tarefa:** Criação do catálogo de strings (`res/values/strings.xml`) e registro do Marco 3 em `docs/STATUS.md`.
- **Subtarefas:**
  1. Criar `app/src/main/res/values/strings.xml` com `<resources>` contendo `app_name` ("Rotina") e strings da interface.
  2. Atualizar `docs/STATUS.md` com o início dos trabalhos do Marco 3.
- **Status:** Em execução.

---

### Antigravity (Maestro)
- **Papel:** Coordenação, revisão de código e garantia de compilação sem concorrência de Gradle.
