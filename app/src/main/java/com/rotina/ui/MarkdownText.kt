package com.rotina.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

import androidx.compose.ui.unit.sp

/**
 * Converte texto Markdown simples (negrito, itálico, código em linha, marcadores de lista)
 * em uma AnnotatedString estilizada nativa do Jetpack Compose.
 */
fun parsearMarkdown(texto: String): AnnotatedString {
    return buildAnnotatedString {
        val linhas = texto.lines()
        linhas.forEachIndexed { indiceLinha, linhaRaw ->
            val linha = linhaRaw.trimEnd()
            
            // Detecta cabeçalhos (ex: ### Título)
            val ehTitulo3 = linha.startsWith("### ")
            val ehTitulo2 = linha.startsWith("## ")
            val ehTitulo1 = linha.startsWith("# ")
            val ehItemLista = linha.startsWith("* ") || linha.startsWith("- ") || linha.startsWith("• ")
            
            val linhaProcessada = when {
                ehTitulo3 -> linha.removePrefix("### ").trim()
                ehTitulo2 -> linha.removePrefix("## ").trim()
                ehTitulo1 -> linha.removePrefix("# ").trim()
                ehItemLista -> "• " + linha.substring(2).trim()
                else -> linha
            }

            val estiloLinha = when {
                ehTitulo1 -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                ehTitulo2 -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                ehTitulo3 -> SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                else -> null
            }

            if (estiloLinha != null) {
                withStyle(estiloLinha) {
                    processarTrechoComEstilos(linhaProcessada, this)
                }
            } else {
                processarTrechoComEstilos(linhaProcessada, this)
            }

            if (indiceLinha < linhas.size - 1) {
                append("\n")
            }
        }
    }
}

/**
 * Processa negrito (**texto**), itálico (*texto*) e código em linha (`código`).
 */
private fun processarTrechoComEstilos(texto: String, builder: AnnotatedString.Builder) {
    var i = 0
    val n = texto.length

    while (i < n) {
        // Negrito: **texto**
        if (i + 1 < n && texto[i] == '*' && texto[i + 1] == '*') {
            val fim = texto.indexOf("**", i + 2)
            if (fim != -1) {
                val conteudo = texto.substring(i + 2, fim)
                builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(conteudo)
                }
                i = fim + 2
                continue
            }
        }

        // Código inline: `código`
        if (texto[i] == '`') {
            val fim = texto.indexOf('`', i + 1)
            if (fim != -1) {
                val conteudo = texto.substring(i + 1, fim)
                builder.withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0x33888888)
                    )
                ) {
                    append(conteudo)
                }
                i = fim + 1
                continue
            }
        }

        // Itálico: *texto* (não seguido por outro *)
        if (texto[i] == '*' && (i + 1 >= n || texto[i + 1] != '*')) {
            val fim = texto.indexOf('*', i + 1)
            if (fim != -1 && (fim + 1 >= n || texto[fim + 1] != '*')) {
                val conteudo = texto.substring(i + 1, fim)
                builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(conteudo)
                }
                i = fim + 1
                continue
            }
        }

        builder.append(texto[i])
        i++
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current
) {
    val annotatedString = remember(text) { parsearMarkdown(text) }
    Text(
        text = annotatedString,
        modifier = modifier,
        color = color,
        style = style
    )
}
