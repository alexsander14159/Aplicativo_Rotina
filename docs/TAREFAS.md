# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Ajuste Crítico de UI: Unificação da Rolagem da Tela Principal (`TelaMetas.kt`)

OpenCode #2 (DEV), siga estas instruções:

### Problema:
Atualmente, `TelaMetas.kt` possui um `Column` fixo externo e um `LazyColumn` apenas para as metas diárias no final da tela. Isso faz com que a lista de metas role separada do restante da tela (gerando duas rolagens independentes e sensação esquisita).

### Solução:
Transformar a tela inteira em um **único `LazyColumn`** (ou `Column` com `verticalScroll(rememberScrollState())`), para que a página toda role de forma contínua e suave, de cima a baixo.

### 1. Em `app/src/main/java/com/rotina/ui/TelaMetas.kt`:
- Substituir o `Column` raiz por um único `LazyColumn` que engloba tudo:
```kotlin
LazyColumn(
    modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    item {
        // Linha com título "Rotina", chip "Folga" e botão "Metas"
    }
    item {
        // Barra de navegação de data (← Anterior / Data 📅 / Seguinte →)
    }
    if (!ehHoje) {
        item {
            // Botão "Hoje"
        }
    }
    item {
        // Card "Resumo do dia"
    }
    item {
        // Card "Treino do Dia" (com botão de anotar treino e editar treinos)
    }
    if (onEditarLembrete != null) {
        item {
            // Botão "Lembrete: HH:mm"
        }
    }
    if (onTesteLembrete != null) {
        item {
            // Botão "Testar lembrete (debug)"
        }
    }
    items(metas, key = { it.id }) { meta ->
        // Linha da Meta + Submetas
    }
}
```

### 2. Compilar e Testar:
Execute uma tarefa por vez:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Instale e teste no celular TECNO KL4:
- Verificar se a tela toda rola de uma só vez (cabeçalho, treino, resumo e metas rolando juntos como uma página única contínua).

### 3. Commit:
```powershell
git add -A
git commit -m "fix: unifica rolagem da tela principal em LazyColumn unico (tela inteira rola junta)"
```

Se houver erro, pare e mostre o erro completo.
