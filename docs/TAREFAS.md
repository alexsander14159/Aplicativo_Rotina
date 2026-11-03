# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Marco 3 — Lembrete configurável + Resumo do dia

### Tarefa 3.1: ✅ Concluída e commitada (`1af1ec6`)
- Horário configurável (padrão 20:00) persistido em SharedPreferences e reagendado no WorkManager.
- Toque na notificação abre a `MainActivity` via `PendingIntent`.
- Verificado em compilação, testes unitários e no celular real (TECNO KL4).

---

### Tarefa 3.2 (Proposta para execução)
- **Objetivo:** Navegação de datas + Tela/Cabeçalho de Resumo do Dia.
- **Responsável:** OpenCode #2 (DEV).
- **Subtarefas:**
  1. No `RotinaViewModel`, transformar a data fixa (`dataDeHoje`) em fluxo observável (`dataSelecionada: MutableStateFlow<Long>`), usando `flatMapLatest` para atualizar reativamente `metas`, `submetas` e `dia`.
  2. Implementar funções no ViewModel: `irParaDiaAnterior()`, `irParaDiaSeguinte()`, `irParaHoje()`.
  3. No topo da tela (`TelaMetas.kt`), exibir seletor/navegador de data ("←", "Hoje, 03/11", "→") e cartão de Resumo: total de metas concluídas / total (ex: "5/6 concluídas • 83%"), além do status de folga.
  4. Testes unitários para a navegação de data e cálculo de resumo.
  5. Compilar, rodar testes e testar no celular.

---

### OpenCode (Auxiliar/Docs)
- **Tarefa:** Manter `docs/STATUS.md` atualizado com o encerramento da Tarefa 3.1 e apoiar na revisão de strings da Tarefa 3.2.
