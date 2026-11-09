# TAREFAS — Rotina App

Atualizado: 2026-11-09

## Marco 6 — Tarefa 6.4: Chat estilo WhatsApp com o Jarvis + Conexão Gemini API + Relatório Diário

OpenCode #2 (DEV), siga estas instruções passo a passo:

### 1. No `app/src/main/AndroidManifest.xml`:
Adicione a permissão de internet (fora de `<application>`):
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

### 2. Criar `app/src/main/java/com/rotina/ia/GeminiClient.kt`:
Cliente HTTPS nativo, sem bibliotecas externas pesadas:
```kotlin
package com.rotina.ia

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {
    private const val MODELO = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    suspend fun enviar(
        chave: String,
        mensagens: List<MensagemPrompt>
    ): Result<String> = withContext(Dispatchers.IO) {
        if (chave.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Chave de API do Gemini não configurada. Toque no ícone de chave/engrenagem para configurar."))
        }

        try {
            val endpoint = "$BASE_URL/$MODELO:generateContent?key=$chave"
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connectTimeout = 30000
                readTimeout = 30000
                doOutput = true
                doInput = true
            }

            // Separa systemInstruction e contents
            val systemMsg = mensagens.firstOrNull { it.papel == "system" }
            val historico = mensagens.filter { it.papel != "system" }

            val payload = JSONObject().apply {
                if (systemMsg != null && systemMsg.texto.isNotBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", systemMsg.texto))
                        })
                    })
                }

                val contentsArray = JSONArray()
                historico.forEach { msg ->
                    val role = if (msg.papel == "user") "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", msg.texto))
                        })
                    })
                }
                put("contents", contentsArray)
            }

            conn.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val json = JSONObject(responseText)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val reply = parts.getJSONObject(0).optString("text", "")
                        return@withContext Result.success(reply.trim())
                    }
                }
                Result.failure(Exception("Resposta vazia da IA."))
            } else {
                val errorText = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                Result.failure(Exception("Erro na API Gemini ($code): $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### 3. No `app/src/main/java/com/rotina/ui/RotinaViewModel.kt`:
Adicione as ações e estados do Jarvis:
```kotlin
    val mensagensChat: StateFlow<List<MensagemIa>> = dao.todasMensagensIa()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val carregandoIa = kotlinx.coroutines.flow.MutableStateFlow(false)
    val erroIa = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    fun enviarMensagemJarvis(context: android.content.Context, texto: String) {
        if (texto.isBlank() || carregandoIa.value) return
        val chave = com.rotina.ia.IaConfig.obterChave(context)
        if (chave.isBlank()) {
            erroIa.value = "Chave da API do Gemini não informada. Toque no ícone de chave para salvar sua chave."
            return
        }

        viewModelScope.launch {
            carregandoIa.value = true
            erroIa.value = null

            // 1. Salva mensagem do usuário
            val dataHoje = dataSelecionada.value
            dao.salvarMensagemIa(
                MensagemIa(data = dataHoje, origem = "USER", conteudo = texto.trim())
            )

            // 2. Coleta contexto e monta prompt
            val perfil = com.rotina.ia.PerfilIa.obter(context)
            val contexto = com.rotina.ia.coletar(dao, dataHoje, perfil)
            val ultimas = dao.ultimasMensagensIa(20).reversed()
            val prompt = com.rotina.ia.montar(contexto, ultimas, texto.trim())

            // 3. Chama a API
            val resultado = com.rotina.ia.GeminiClient.enviar(chave, prompt)
            resultado.fold(
                onSuccess = { resposta ->
                    dao.salvarMensagemIa(
                        MensagemIa(data = dataHoje, origem = "ASSISTANT", conteudo = resposta)
                    )
                },
                onFailure = { err ->
                    erroIa.value = err.localizedMessage ?: "Erro ao comunicar com a IA."
                }
            )
            carregandoIa.value = false
        }
    }

    fun pedirRelatorioDoDia(context: android.content.Context) {
        if (carregandoIa.value) return
        val chave = com.rotina.ia.IaConfig.obterChave(context)
        if (chave.isBlank()) {
            erroIa.value = "Chave da API do Gemini não informada. Configure no ícone de chave."
            return
        }

        viewModelScope.launch {
            carregandoIa.value = true
            erroIa.value = null

            val dataHoje = dataSelecionada.value
            val perfil = com.rotina.ia.PerfilIa.obter(context)
            val contexto = com.rotina.ia.coletar(dao, dataHoje, perfil)
            val ultimas = dao.ultimasMensagensIa(10).reversed()
            val prompt = com.rotina.ia.montar(contexto, ultimas, pergunta = null)

            val resultado = com.rotina.ia.GeminiClient.enviar(chave, prompt)
            resultado.fold(
                onSuccess = { resposta ->
                    dao.salvarMensagemIa(
                        MensagemIa(data = dataHoje, origem = "ASSISTANT", conteudo = resposta)
                    )
                },
                onFailure = { err ->
                    erroIa.value = err.localizedMessage ?: "Erro ao gerar relatório."
                }
            )
            carregandoIa.value = false
        }
    }

    fun limparErroIa() {
        erroIa.value = null
    }
```

---

### 4. Criar a tela de Chat `app/src/main/java/com/rotina/ui/TelaChatJarvis.kt`:
- **TopBar:** Título "Jarvis (Mentor)", botão "Voltar", botão "Relatório do Dia" e botão para configurar a chave do Gemini.
- **Lista de Mensagens (estilo WhatsApp):**
  - Mensagem do Usuário: alinhada à direita, fundo destacado (`primaryContainer`).
  - Mensagem do Jarvis: alinhada à esquerda, fundo suave (`surfaceVariant`).
  - Rolagem automática para a última mensagem.
- **Indicador de carregamento:** quando `carregandoIa == true`, mostrar "Jarvis está pensando...".
- **Mensagem de erro:** exibir `Snackbar` ou texto de alerta se `erroIa != null`.
- **Barra de envio inferior:**
  - Campo de texto `OutlinedTextField` com botão de enviar.
  - Botão de microfone para ditar mensagens por voz via `SpeechRecognizer`.
- **Diálogo para Chave de API:**
  - Diálogo com campo de texto protegido (`PasswordVisualTransformation` ou texto) para colar a chave do Gemini e salvar via `IaConfig.salvarChave(context, chave)`.

---

### 5. Integrar na Tela Principal (`TelaMetas.kt`) e `MainActivity.kt`:
- Adicionar botão em destaque na tela principal: **"💬 Conversar com o Jarvis"** (`onAbrirChat: () -> Unit`).
- Navegar para `TelaChatJarvis` quando acionado.

---

### 6. Compilar e Testar:
Execute uma tarefa por vez:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Instale e teste no TECNO KL4:
- Abrir o chat do Jarvis.
- Configurar uma chave de teste ou validar a mensagem caso a chave não esteja definida.
- Testar layout dos balões estilo mensageiro.

### 7. Commit:
```powershell
git add -A
git commit -m "feat: tela de chat estilo mensageiro com o Jarvis e cliente Gemini API (tarefa 6.4)"
```

Se houver erro, pare e mostre o erro completo.
