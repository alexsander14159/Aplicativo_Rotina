# Rotina

Aplicativo Android nativo para organizar a rotina diária, registrar treinos e acompanhar a evolução com o assistente **Jarvis**.

> Versão atual: **0.1.0** (`versionCode 1`) — Marcos 1 a 7 concluídos e validados no aparelho físico TECNO KL4 (Android 14).

![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room-v8-3DDC84)
![WorkManager](https://img.shields.io/badge/WorkManager-2.12-3DDC84)

## Funcionalidades

- **Checklist diário de metas** com submetas, alvo diário, folga do dia e progresso visual.
- **Navegação por calendário** para consultar e registrar qualquer data sem perder o histórico.
- **Lembretes configuráveis** com `WorkManager` e abertura do app pela notificação.
- **Treino da semana** com grade de segunda a domingo e registro do treino realizado.
- **Registro de exercícios** com séries, repetições e carga, além de cardio (tipo e minutos).
- **Diário por voz** usando o reconhecedor nativo do Android e **notas rápidas** ao longo do dia.
- **Jarvis com IA**: chat com histórico, contexto do dia e relatório diário personalizado.
- **Tema Material 3** com paletas Dark Slate e Light, tipografia, formas e componentes próprios.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| Banco local | Room v8 |
| Tarefas em background | WorkManager 2.12 |
| Voz | `SpeechRecognizer` nativo (pt-BR) |
| IA | Gemini API (`gemini-3.1-flash-lite`) |
| Build | AGP 9.4.0, Gradle 9.6.0, JDK 17 |
| Android | `minSdk 26`, `targetSdk 37`, `compileSdk 37` |

## Como compilar

Pré-requisitos:

- JDK 17
- Android SDK 37
- Android Studio (recomendado) ou Gradle configurado

```bash
# Linux/macOS
./gradlew assembleDebug

# Windows
.\gradlew.bat assembleDebug
```

O APK de debug é gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para rodar os testes JVM:

```bash
./gradlew testDebugUnitTest
```

> O arquivo `local.properties` contém o caminho do SDK e é ignorado pelo Git. Configure-o pelo Android Studio ou crie o arquivo localmente com `sdk.dir=...`.

## Configuração da IA

O aplicativo **não inclui** uma chave de API.

1. Abra a tela do **Jarvis** no app.
2. Toque no ícone de configuração da chave.
3. Cole uma chave do [Google AI Studio](https://aistudio.google.com/).

A chave fica em `SharedPreferences` privado no aparelho (`rotina_ia_prefs`) e não é versionada. Ao usar o chat ou o Relatório do Dia, o contexto necessário é enviado diretamente à API Gemini. O histórico do chat permanece no banco local do app.

## Estrutura do projeto

```text
app/src/main/java/com/rotina/
├── audio/       # Reconhecimento de voz
├── data/        # Room, entidades, DAOs e repositórios
├── ia/          # Cliente Gemini, configuração e contexto
├── notif/       # Notificações e WorkManager
└── ui/          # Telas, componentes, tema e ViewModel
```

A documentação de planejamento e acompanhamento fica em [`docs/`](docs):

- [`docs/PLANO.md`](docs/PLANO.md)
- [`docs/TAREFAS.md`](docs/TAREFAS.md)
- [`docs/STATUS.md`](docs/STATUS.md)

## Validação

- **45/45** testes JVM passando.
- `assembleDebug` executado com sucesso.
- Instalação e uso validados no **TECNO SPARK GO 1 (KL4)**, Android 14.

## Segurança e privacidade

- Nenhuma chave de API, senha ou keystore é versionada.
- `local.properties`, builds, caches e arquivos de assinatura ficam fora do Git.
- O app não possui conta própria, anúncios ou servidor de dados pessoais.
- O envio de conteúdo para a IA acontece somente quando o usuário usa os recursos do Jarvis.

## Status

Marcos 1 a 7 concluídos. O projeto está em versão **0.1.0** e a próxima etapa depende de uma nova especificação.
