# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Marco 4 — Tarefa 4.2: Exibição e Edição dos Treinos da Semana

OpenCode #2 (DEV), siga estas instruções passo a passo:

### 1. No `app/src/main/java/com/rotina/ui/RotinaViewModel.kt`:
- Adicionar o fluxo reativo do treino do dia correspondente à `dataSelecionada`:
```kotlin
    val treinoDoDia: StateFlow<TreinoSemana?> = dataSelecionada
        .flatMapLatest { epoch ->
            val diaSemana = LocalDate.ofEpochDay(epoch).dayOfWeek.value // 1 (Segunda) a 7 (Domingo)
            dao.treinoDoDia(diaSemana)
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val todosTreinos: StateFlow<List<TreinoSemana>> = dao.todosTreinos()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun salvarTreino(diaSemana: Int, titulo: String, descricao: String) {
        viewModelScope.launch {
            dao.salvarTreino(TreinoSemana(diaSemana = diaSemana, titulo = titulo, descricao = descricao))
        }
    }
```

### 2. Criar a tela/diálogo de edição dos treinos da semana `app/src/main/java/com/rotina/ui/TelaTreinosEdit.kt`:
- Apresentar os 7 dias da semana (Segunda-feira, Terça-feira, Quarta-feira, Quinta-feira, Sexta-feira, Sábado, Domingo).
- Para cada dia:
  - Exibir o dia da semana.
  - Campos editáveis para `titulo` (ex: "Peito e Tríceps", "Descanso") e `descricao` (exercícios ou notas).
  - Botão "Salvar" que chama `onSalvar(diaSemana, titulo, descricao)`.
- Botão no topo para "Voltar" (`onVoltar: () -> Unit`).

### 3. Em `app/src/main/java/com/rotina/ui/TelaMetas.kt`:
- Adicionar o card "Treino do Dia" (acima ou abaixo do resumo de metas):
  - Exibe o título do treino do dia da semana atual (ou "Nenhum treino definido para hoje / Descanso" se null ou vazio).
  - Se houver descrição, exibe a descrição formatada.
  - Botão "Editar Treinos da Semana" que chama o callback `onEditarTreinos: () -> Unit`.

### 4. Em `app/src/main/java/com/rotina/MainActivity.kt`:
- Integrar a navegação de estado: se estiver no modo de edição de treinos (`modoTreinosEdit`), exibir `TelaTreinosEdit`; caso contrário, exibir `TelaMetas`.

### 5. Compilar e Testar:
Execute uma tarefa por vez:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Instale e teste no celular real (TECNO KL4):
- Verificar se o card aparece na tela principal.
- Tocar em "Editar Treinos", cadastrar um treino para o dia de hoje (ex: Terça-feira).
- Voltar e conferir se o card atualizou imediatamente.

### 6. Commit:
```powershell
git add -A
git commit -m "feat: exibicao do treino do dia e tela de edicao semanal (tarefa 4.2)"
```

Se houver erro, pare e mostre o erro completo.
