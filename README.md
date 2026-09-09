# ☀️ Clima Tempo (Weather & Climate App)

> **Aplicativo meteorológico inteligente, visual e multilíngue para Android construído com Jetpack Compose, Gemini AI e dados em tempo real da Open-Meteo.**

[![Android](https://img.shields.io/badge/Plataforma-Android%2014%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![AI](https://img.shields.io/badge/IA-Gemini%203.5%20Flash-orange.svg)](https://ai.google.dev)
[![Licença](https://img.shields.io/badge/Licença-MIT-blue.svg)](LICENSE)

---

## 📑 Sumário

1. [Visão Geral](#-visão-geral)
2. [Principais Funcionalidades](#-principais-funcionalidades)
3. [Suporte Multilíngue (PT-BR, ES, EN)](#-suporte-multilíngue)
4. [Inteligência Artificial Humanizada (Clima AI)](#-inteligência-artificial-humanizada-clima-ai)
5. [Arquitetura & Engenharia do Projeto](#-arquitetura--engenharia-do-projeto)
6. [Tecnologias Utilizadas](#-tecnologias-utilizadas)
7. [Como Executar o Projeto](#-como-executar-o-projeto)
8. [Configuração de Chaves (API Keys)](#-configuração-de-chaves-api-keys)
9. [Guia de Uso para Clientes e Usuários Finais](#-guia-de-uso-para-clientes-e-usuários-finais)
10. [Testes Automatizados](#-testes-automatizados)

---

## 🌟 Visão Geral

O **Clima Tempo** é um aplicativo mobile moderno projetado para oferecer previsões meteorológicas precisas, hiper-visuais e intuitivas. Desenvolvido sob as diretrizes do **Material Design 3 (M3)**, o app adapta gradientes dinâmicos conforme as condições do céu (ensolarado, nublado, chuvoso, tempestade) e o horário do dia (dia ou noite), proporcionando uma experiência imersiva e agradável.

Além dos dados tradicionais de temperatura, sensação térmica, umidade e vento, o app conta com o **Clima AI**, um assistente que se comunica de maneira **humana, calorosa e prática**, auxiliando na escolha de vestuário, práticas esportivas e cuidados preventivos.

---

## 🚀 Principais Funcionalidades

- 📍 **Localização em Tempo Real (GPS)**: Detecção automática da cidade atual do usuário com permissões em tempo de execução.
- 🔍 **Busca Global de Cidades**: Autocomplete rápido com geocodificação mundial, trazendo cidades com seus respectivos estados e países.
- ⭐ **Cidades Favoritas & Recentes**: Salve múltiplos locais com persistência offline usando o **Room Database**.
- ⏱️ **Previsão Horária (24 Horas)**: Carrossel com temperaturas a cada hora, ícones do tempo e probabilidades de chuva.
- 📅 **Previsão Estendida para 7 Dias**: Visualização com barras térmicas proporcionais (mínima e máxima) e índice de precipitação.
- 📊 **Métricas Atmosféricas Detalhadas**:
  - Sensação térmica e comparação com a temperatura real.
  - Umidade relativa do ar com níveis de conforto.
  - Velocidade e direção cardeal do vento (N, NE, L, S, SO, etc.).
  - Índice de radiação UV e alertas de intensidade.
  - Horários exatos do nascer e pôr do sol.
- 🎨 **Transições Visuais Fluidas**: Fundo reativo com animações suaves de degradê e ícones vetoriais modernos.

---

## 🌐 Suporte Multilíngue

O aplicativo foi projetado para atender o público da América Latina, Espanha e internacional, com suporte prioritário para:

| Idioma | Bandeira | Descrição |
| :--- | :---: | :--- |
| **Português (Brasil)** | 🇧🇷 | Idioma padrão com termos e expressões regionais naturais |
| **Español** | 🇪🇸 | Tradução completa para o público hispânico |
| **English** | 🇺🇸 | Tradução universal para usuários internacionais |

O usuário pode alternar o idioma **a qualquer momento** usando o seletor rápido no topo da tela principal, sem necessidade de reiniciar o aplicativo.

---

## 🤖 Inteligência Artificial Humanizada (Clima AI)

Diferente de sistemas robóticos convencionais, o **Clima AI** foi instruído para agir como um **amigo próximo e atencioso**:
- **Tom Acolhedor**: Respostas gentis, empáticas e práticas.
- **Dicas de Vestuário**: Recomendações de roupas leves, casacos pesados ou capas de chuva com base nos graus e sensação térmica.
- **Planejamento de Atividades**: Orientações seguras para caminhadas, corridas e passeios ao ar livre.
- **Perguntas Rápidas (Chips)**: Toque direto para dúvidas frequentes (*"O que vestir hoje?"*, *"Vai chover mais tarde?"*, *"É bom para correr ao ar livre?"*).

---

## 🏗️ Arquitetura & Engenharia do Projeto

O projeto segue os princípios de **Clean Architecture** e **MVVM (Model-View-ViewModel)** recomendados pelo Google:

```text
com.example
├── data
│   ├── api          # Consumo REST da Open-Meteo & Google Gemini API
│   ├── db           # Banco de dados local Room (Entity, DAO, Database)
│   ├── location     # Provedor de localização via FusedLocationProviderClient
│   └── repository   # WeatherRepository (Única fonte de verdade)
├── ui
│   ├── components   # Componentes modulares Jetpack Compose (Cards, Sheets, Carousel)
│   ├── model        # Modelos de UI (WeatherDetails, DailyForecast, AppLanguage)
│   ├── theme        # Cores, Tipografia e Sistema de Design Material 3
│   ├── util         # WeatherLocalization (Gestor central de localização)
│   ├── WeatherScreen.kt    # Tela e navegação principal
│   └── WeatherViewModel.kt # Gerenciamento de estado reativo (StateFlow)
└── MainActivity.kt  # Ponto de entrada da aplicação
```

---

## 🛠️ Tecnologias Utilizadas

- **Kotlin & Coroutines/Flow**: Programação reativa assíncrona para chamadas de rede e banco de dados.
- **Jetpack Compose**: Interface declarativa com suporte a Material Design 3.
- **Room Persistence Library**: Armazenamento SQLite local de cidades favoritas.
- **Retrofit & Moshi**: Comunicação REST eficiente e desserialização JSON tipada.
- **Open-Meteo API**: Dados meteorológicos globais em tempo real sem necessidade de chaves pagas.
- **Google Gemini API**: Modelos de linguagem de última geração para assistência contextual.
- **Robolectric & Roborazzi**: Testes de unidade rápidos na JVM e testes visuais de regressão de tela.

---

## 💻 Como Executar o Projeto

### Pré-requisitos
1. **Android Studio Ladybug / Jellyfish (ou mais recente)** instalado.
2. **JDK 17 ou 21** configurado.
3. Dispositivo físico ou Emulador com **Android 8.0 (API 26) ou superior**.

### Passos
```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/clima-tempo.git

# 2. Acesse a pasta do projeto
cd clima-tempo

# 3. Compile e execute os testes de unidade
./gradlew testDebugUnitTest

# 4. Instale o APK no dispositivo conectado
./gradlew installDebug
```

---

## 🔑 Configuração de Chaves (API Keys)

O aplicativo consome previsões da Open-Meteo sem exigir chaves. Para ativar os recursos avançados de IA com o **Google Gemini**:

1. Crie um arquivo `.env` na raiz do projeto (copie a partir de `.env.example`):
   ```env
   GEMINI_API_KEY=sua_chave_aqui
   ```
2. Caso não configure uma chave, o aplicativo ativa automaticamente o **Modo Inteligente Local**, garantindo respostas e dicas funcionais mesmo sem conexão com a API da Gemini.

---

## 👥 Guia de Uso para Clientes e Usuários Finais

1. **Permissão de Localização**: Ao abrir pela primeira vez, autorize a localização para visualizar automaticamente o clima da sua rua ou bairro.
2. **Ver Outras Cidades**: Toque no ícone de lupa 🔍 ou no nome da cidade para buscar qualquer cidade do planeta.
3. **Salvar Favoritas**: Toque no ícone de coração ❤️ para salvar a cidade na sua lista de acesso rápido.
4. **Mudar Idioma**: No topo da tela, toque nas opções 🇧🇷 **PT**, 🇪🇸 **ES** ou 🇺🇸 **EN** para alternar a língua em tempo real.
5. **Conversar com o Clima AI**: Toque no botão flutuante **Clima AI** no canto inferior direito para tirar qualquer dúvida sobre o clima com o assistente inteligente.

---

## 🧪 Testes Automatizados

O projeto conta com uma suíte de testes na JVM para garantir estabilidade contínua:
- `ExampleRobolectricTest`: Validação de recursos de strings, mapeamento de códigos meteorológicos WMO e precisão das traduções em português, espanhol e inglês.
- `GreetingScreenshotTest`: Teste de regressão visual com Roborazzi para garantir a fidelidade do layout em telas de diferentes densidades (ex: Pixel 8).

Para rodar os testes:
```bash
./gradlew :app:testDebugUnitTest
```

---

*Desenvolvido com foco em excelência visual, performance e usabilidade.*
