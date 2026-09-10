# Clima Tempo — previsão do tempo nativa Android com IA

App de previsão do tempo com localização em tempo real, busca de qualquer cidade do mundo,
previsão horária e de 7 dias, e um assistente de IA (Gemini) que dá conselho prático sobre o
clima em vez de só mostrar número. Kotlin + Jetpack Compose.

## Índice

- [O que o app faz](#o-que-o-app-faz)
- [Clima AI: como o assistente funciona de verdade](#clima-ai-como-o-assistente-funciona-de-verdade)
- [Como rodar](#como-rodar)
- [Arquitetura](#arquitetura)
- [Testes](#testes)
- [Estrutura de arquivos](#estrutura-de-arquivos)
- [O que é MVP de propósito](#o-que-é-mvp-de-propósito)

## O que o app faz

- **Localização em tempo real** (GPS) com pedido de permissão em runtime, e **busca global de
  cidade** por nome, com estado/país.
- **Cidades favoritas e recentes**, salvas localmente (Room), sem depender de conta ou nuvem.
- **Previsão horária (24h)** e **estendida (7 dias)**, com temperatura mínima/máxima, chance de
  chuva, umidade, vento (velocidade e direção cardeal), índice UV, nascer e pôr do sol.
- **Dados meteorológicos via Open-Meteo**, API pública sem necessidade de chave paga.
- **3 idiomas** (Português, Espanhol, Inglês), trocável a qualquer momento sem reiniciar o app.
- **Clima AI**: assistente que responde em linguagem natural (ex.: "o que vestir hoje?", "vai
  chover mais tarde?") usando os dados reais da previsão carregada.

## Clima AI: como o assistente funciona de verdade

O assistente usa a API do Gemini quando há uma chave configurada (`GEMINI_API_KEY` no `.env`) e
a chamada de rede funciona. Isso é verificado no código, não é suposição: sem chave configurada,
com a chave de exemplo do `.env.example`, ou se a chamada à API falhar por qualquer motivo
(sem internet, erro do servidor), o app cai automaticamente num **motor de heurística local**
(`generateLocalHumanAdvice`, em `GeminiService.kt`) que continua dando conselho de vestuário e
atividade a partir dos números reais da previsão — só não é gerado por um modelo de linguagem
nesse modo. Ou seja: o assistente nunca fica mudo, com ou sem IA de verdade configurada.

## Como rodar

App Android nativo (Kotlin/Jetpack Compose), não tem versão web. Pra rodar:

```bash
# Android Studio (recomendado): abrir a pasta do projeto e rodar num emulador ou aparelho.
# Ou via linha de comando, com Android SDK instalado e configurado:
./gradlew assembleDebug     # gera o APK de debug
./gradlew installDebug      # instala no aparelho/emulador conectado
```

Precisa de JDK 17+ e Android SDK (compileSdk 36) instalados. Sem eles, o Gradle não compila —
mesmo os testes locais (Robolectric) dependem do SDK pra achar os stubs de `android.jar`.

Pra ativar o Clima AI com o Gemini de verdade (opcional, o app funciona sem isso):

```env
# .env, copiado de .env.example
GEMINI_API_KEY=sua_chave_aqui
```

## Arquitetura

MVVM: `WeatherViewModel` mantém o estado da tela em `StateFlow`, delega busca de dados ao
`WeatherRepository`, que combina a API pública Open-Meteo (`OpenMeteoService`, via Retrofit) com
o cache local de cidades favoritas (Room). O Clima AI é uma chamada separada
(`GeminiService`) que só entra quando o usuário abre o assistente.

```
com.example
├── data
│   ├── api          Open-Meteo (previsão) e Gemini (assistente), via Retrofit/OkHttp
│   ├── db           Room: cidades salvas
│   ├── location     FusedLocationProviderClient
│   └── repository   WeatherRepository, fonte única de verdade da tela
├── ui
│   ├── components   cards, carrossel horário, folha de busca, folha do Clima AI
│   ├── model        modelos de UI e o enum WeatherType (mapeia código WMO -> ícone/descrição)
│   ├── theme        Material 3
│   ├── util         WeatherLocalization (PT/ES/EN)
│   ├── WeatherScreen.kt
│   └── WeatherViewModel.kt
└── MainActivity.kt
```

## Testes

`ExampleRobolectricTest.kt` tem 3 testes reais (não é só o boilerplate padrão do Android
Studio): verifica se o código meteorológico WMO da Open-Meteo mapeia pro texto certo em
português, espanhol e inglês (`WeatherType.fromWmo`), e se `WeatherLocalization.getTodayLabel`
traduz certo nos três idiomas. `GreetingScreenshotTest.kt` também é real, apesar do nome
genérico: captura uma screenshot do `CurrentWeatherCard` de verdade com dado de exemplo, via
Roborazzi. Só `ExampleUnitTest.kt` (2+2=4) é puro boilerplate sem relação com o app.

Este ambiente de desenvolvimento não consegue baixar o Android SDK, então não dá pra rodar
`./gradlew testDebugUnitTest` aqui pra confirmar que os 3 testes reais passam — isso precisa
ser verificado num Android Studio local antes de qualquer entrega.

## Estrutura de arquivos

Ver diagrama em [Arquitetura](#arquitetura) acima — é a estrutura real de pastas do projeto.

## O que é MVP de propósito

- **Sem cache de previsão offline**: a tela precisa de internet pra buscar dado novo (cidades
  favoritas ficam salvas, a previsão em si não).
- **Sem push notification** de mudança brusca de clima ou alerta de tempestade.
- **Cobertura de teste real, mas parcial**: cobre mapeamento de código WMO e tradução, não
  cobre `WeatherRepository` nem o fluxo de fallback do Clima AI.
