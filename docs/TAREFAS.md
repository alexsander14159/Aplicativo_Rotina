# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Marco 4 — Tarefa 4.4: Interface do Diário de Treino Realizado (Cargas, Séries, Cardio e Alterações)

OpenCode #2 (DEV), siga estas instruções passo a passo:

### 1. No `app/src/main/java/com/rotina/ui/RotinaViewModel.kt`:
Adicione as conexões reativas e ações:
```kotlin
    val registroTreinoDoDia: StateFlow<RegistroTreinoDia?> = dataSelecionada
        .flatMapLatest { dao.registroTreinoDoDia(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val exerciciosDoDia: StateFlow<List<ExercicioRegistro>> = dataSelecionada
        .flatMapLatest { dao.exerciciosDoDia(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun salvarRegistroTreino(
        concluido: Boolean,
        alteracoes: String,
        fezCardio: Boolean,
        tipoCardio: String,
        cardioMinutos: Int
    ) {
        viewModelScope.launch {
            dao.salvarRegistroTreino(
                RegistroTreinoDia(
                    data = dataSelecionada.value,
                    concluido = concluido,
                    alteracoesRotina = alteracoes,
                    fezCardio = fezCardio,
                    tipoCardio = tipoCardio,
                    cardioMinutos = cardioMinutos
                )
            )
        }
    }

    fun adicionarExercicio(nome: String, series: Int, reps: String, carga: String) {
        viewModelScope.launch {
            val proximaOrdem = (exerciciosDoDia.value.maxOfOrNull { it.ordem } ?: 0) + 1
            dao.salvarExercicio(
                ExercicioRegistro(
                    data = dataSelecionada.value,
                    nome = nome,
                    series = series,
                    repeticoes = reps,
                    carga = carga,
                    ordem = proximaOrdem
                )
            )
        }
    }

    fun apagarExercicio(id: Long) {
        viewModelScope.launch {
            dao.apagarExercicio(id)
        }
    }
```

### 2. Criar a tela `app/src/main/java/com/rotina/ui/TelaRegistroTreino.kt`:
- Título da tela com a data atual formatada.
- Checkbox / Switch: "Treino do dia concluído".
- Campo de texto (OutlinedTextField): "Alterações na rotina / Observações" (ex: "Treino foi mais curto por falta de tempo", etc.).
- **Bloco de Cardio:**
  - Checkbox: "Fez cardio hoje?"
  - Se marcado, exibir campos: "Tipo de cardio" (ex: Esteira, Bicicleta, Caminhada) e "Duração em minutos" (numérico).
- **Bloco de Exercícios Realizados:**
  - Lista dos exercícios já salvos no dia com nome, séries, repetições, carga (ex: "Supino Reto — 4 séries • 10 reps • 30 kg") e botão para apagar.
  - Botão ou diálogo "Adicionar Exercício" com os campos: Nome do exercício, Séries (número), Repetições (texto, ex: "10-10-8"), Carga/Peso (texto, ex: "25kg cada lado") e botão Adicionar.
- Botão "Salvar e Voltar" (salva os campos gerais de registroTreino e chama `onVoltar`).

### 3. Em `app/src/main/java/com/rotina/ui/TelaMetas.kt`:
- No card "Treino do Dia":
  - Se houver registro no dia, mostrar resumo amigável (ex: badge ou texto "✅ Treino Concluído" se `concluido == true`, "Exercícios: X anotados", "Cardio: 20 min (Esteira)").
  - Adicionar botão com destaque: "Anotar Treino / Cargas" (`onAnotarTreino: () -> Unit`).

### 4. Em `app/src/main/java/com/rotina/MainActivity.kt`:
- Adicionar estado `modoRegistroTreino = remember { mutableStateOf(false) }`.
- Gerenciar navegação: se `modoRegistroTreino == true`, exibir `TelaRegistroTreino`; caso contrário, exibir `TelaMetas`.

### 5. Compilar e Testar:
Execute uma tarefa por vez:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Instale e teste no celular real (TECNO KL4):
- Tocar em "Anotar Treino / Cargas".
- Marcar treino concluído, adicionar 1 exercício com séries, reps e carga.
- Marcar cardio (20 min Esteira).
- Salvar e voltar: verificar se o card na tela principal reflete os dados salvos.

### 6. Commit:
```powershell
git add -A
git commit -m "feat: tela de registro de treino com series, cargas, cardio e alteracoes (tarefa 4.4)"
```

Se houver erro, pare e mostre o erro completo.
