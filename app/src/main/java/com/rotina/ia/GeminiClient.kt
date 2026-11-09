package com.rotina.ia

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Cliente HTTPS mínimo da Gemini API. A chave fica no header, nunca na URL nem em logs. */
object GeminiClient {
    private const val MODELO = "gemini-3.1-flash-lite"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    suspend fun enviar(
        chave: String,
        mensagens: List<MensagemPrompt>,
    ): Result<String> = withContext(Dispatchers.IO) {
        if (chave.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException(
                    "Chave de API do Gemini não configurada. " +
                        "Toque no ícone de chave/engrenagem para configurar.",
                ),
            )
        }

        var conn: HttpURLConnection? = null
        try {
            val endpoint = "$BASE_URL/$MODELO:generateContent"
            conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("x-goog-api-key", chave)
                connectTimeout = 30000
                readTimeout = 30000
                doOutput = true
                doInput = true
            }

            val systemMsg = mensagens.firstOrNull { it.papel == "system" }
            val historico = mensagens.filter { it.papel != "system" }

            val payload = JSONObject().apply {
                if (systemMsg != null && systemMsg.texto.isNotBlank()) {
                    put(
                        "systemInstruction",
                        JSONObject().apply {
                            put(
                                "parts",
                                JSONArray().apply {
                                    put(JSONObject().put("text", systemMsg.texto))
                                },
                            )
                        },
                    )
                }

                val contentsArray = JSONArray()
                historico.forEach { msg ->
                    val role = if (msg.papel == "user") "user" else "model"
                    contentsArray.put(
                        JSONObject().apply {
                            put("role", role)
                            put(
                                "parts",
                                JSONArray().apply {
                                    put(JSONObject().put("text", msg.texto))
                                },
                            )
                        },
                    )
                }
                put("contents", contentsArray)
            }

            conn.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val resposta = extrairResposta(responseText)
                if (resposta.isNullOrBlank()) {
                    Result.failure(Exception("Resposta vazia da IA."))
                } else {
                    Result.success(resposta.trim())
                }
            } else {
                val errorText = conn.errorStream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    .orEmpty()
                Result.failure(Exception("Erro na API Gemini ($code): $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    /** Extrai o primeiro texto de candidates[0].content.parts. Separado para teste e leitura. */
    internal fun extrairResposta(responseText: String): String? {
        val json = JSONObject(responseText)
        val candidates = json.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null
        return parts.getJSONObject(0).optString("text", "")
    }
}
