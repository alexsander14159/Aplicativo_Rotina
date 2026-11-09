# STATUS — Rotina App

Atualizado: 2026-11-09

## Estado atual: Marcos 1 a 6 CONCLUÍDOS e VALIDADOS no celular físico TECNO KL4 ✅
**Próximo passo:** Marco 7 — Design & UI/UX Polish (Embelezamento visual, Material 3, tema escuro, ícones e acabamento).

---

## 📱 Dispositivo & Ambiente
- **Aparelho físico:** TECNO SPARK GO 1 (KL4), Android 14 (API 34)
- **SDK:** `minSdk = 26`, `targetSdk = 37` (Android 15), `compileSdk = 37`
- **Stack:** Kotlin nativo + Jetpack Compose (Material 3) + Room v8 + WorkManager 2.12 + Gemini API (gemini-3.1-flash-lite)
- **Testes automatizados:** 41/41 testes JVM passando.

---

## 👥 Agentes
| Terminal | Modelo | Papel |
|---|---|---|
| Antigravity (maestro) | Gemini Flash / Claude Sonnet | Maestro: planejamento, pesquisa, delegação e revisão |
| OpenCode #2 | GLM-5.3-Flash / DeepSeek | DEV principal: implementação e testes |
| OpenCode | Nemotron gratuito | Ajudante leve: textos, documentação, revisão de leitura |

---

## 🏆 Resumo dos Marcos Entregues (100% Funcionais no Aparelho)

### Marco 1: Base e Checklist Diário
- Room Database com seed inicial de 6 metas.
- Checklist diário com persistência em `ChecklistDia` (chave única por `data` e `metaId`).
- Ruling do `@Upsert` por PK e proteção contra cliques rápidos com `Mutex`.
- Lembrete diário com WorkManager e canal de notificação com ícone (`ic_notificacao`).

### Marco 2: Metas Customizáveis e Submetas
- CRUD de metas: criar, renomear, mover e apagar.
- Suporte a submetas com marcação diária independente.
- Alvo diário em texto (ex: "2h de estudo", "8 copos").
- Chip "Folga do dia" com registro por data.

### Marco 3: Lembrete Configurável e Navegação Temporal
- Seletor de horário do lembrete (TimePicker) persistido em SharedPreferences e reagendado com `CANCEL_AND_REENQUEUE`.
- Clique na notificação abre o app via `PendingIntent`.
- Calendário mensal com seletor `DatePickerDialog` ao tocar na data: salta para qualquer dia em 1 clique.
- Botão dinâmico "Hoje" para voltar ao dia atual.
- Card "Resumo do Dia" com percentual e contagem de metas cumpridas.
- Rolagem unificada em `LazyColumn` único da tela inteira.

### Marco 4: Treino da Semana e Diário de Treino Realizado
- Grade semanal de treinos planejados (Segunda a Domingo).
- Card "Treino do Dia" que muda automaticamente conforme o dia da semana navegado no calendário.
- Tela de anotação de treino realizado (`TelaRegistroTreino.kt`):
  - Checkbox de treino concluído.
  - Campo de texto para alterações da rotina / observações.
  - Lista dinâmica de exercícios com **Séries**, **Repetições** e **Carga/Peso** (ex: *Supino Reto — 4 séries • 10 reps • 30 kg*).
  - Seção de Cardio: ativação, tipo (ex: *Esteira*) e minutos.
  - Room v4 com isolamento estrito por data.

### Correção de Vigência Temporal (Migration 4 → 5)
- Adicionados `dataInicio` e `dataFim` em `metas_dia` e `submetas`.
- Metas criadas em datas futuras (ex: 05/11) **não aparecem** em dias anteriores (04/11, 03/11).
- Soft-delete: encerrar meta no presente preserva o histórico intacto no passado.

### Marco 5: Diário por Áudio Nativo e Notas de Pouco em Pouco
- Transcrição nativa de voz usando `SpeechRecognizer` do Android em português (`pt-BR`), offline, privado e sem custos.
- Permissão `RECORD_AUDIO` em tempo de execução.
- Card "Momentos & Diário do Dia": feed com anotações soltas ao longo do dia, gravando horário exato (ex: `07:49 — 1ª refeição concluída`).
- Room v8 (`notas_dia`).

### Marco 6: Assistente Jarvis (IA)
- Tela de Chat interativa estilo WhatsApp com balões de conversa e histórico perpétuo (`mensagens_ia`).
- Diálogo seguro para colar a Chave da API Gemini (armazenada de forma estritamente privada em `IaConfig`, nunca exposta).
- Motor de Contexto (`ContextoIa.kt`): reúne metas batidas, treino/cargas/cardio e momentos do dia.
- Conexão HTTPS nativa com a API Gemini usando o modelo `gemini-3.1-flash-lite`.
- Botão "Relatório do Dia": briefing diário personalizado com tom de mentor.
- Testado e validado fisicamente no celular TECNO KL4 com resposta real gerada pelo Jarvis!

---

## 📌 Próxima Tarefa para o Novo Terminal:
- **Marco 7 — Design & UI/UX Polish:**
  1. Definir paleta de cores moderna Material 3 (Dark/Light).
  2. Adicionar ícones visuais para metas, treino, água e diário.
  3. Melhorar espaçamentos, cantos arredondados e elevação dos cards.
  4. Renderizar formatação Markdown nos balões do Jarvis.
