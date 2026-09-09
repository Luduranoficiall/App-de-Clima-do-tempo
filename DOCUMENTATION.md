# 📘 Documentação Técnica e Funcional do Sistema (Clima Tempo)

Este documento foi elaborado para fornecer uma visão abrangente sobre o funcionamento do aplicativo **Clima Tempo**, detalhando o fluxo de dados, a camada de negócio, integrações externas e as diretrizes de expansão.

---

## 1. Escopo e Propósito do Produto

O **Clima Tempo** é uma solução mobile Android desenvolvida para fornecer previsões meteorológicas em tempo real com alta fidelidade visual, suporte a múltiplos idiomas (Português, Espanhol e Inglês) e assistência contextual baseada em Inteligência Artificial Generativa.

### Objetivos de Negócio:
1. **Engajamento**: Interface visual moderna e responsiva com degradês reativos à hora do dia e condição climática.
2. **Acessibilidade Global**: Experiência nativa em três línguas sem atritos de recarregamento.
3. **Utilidade Prática**: O assistente Clima AI transforma dados brutos (temperatura, umidade, vento) em recomendações práticas do dia a dia (o que vestir, cuidados com saúde, planejamento esportivo).

---

## 2. Diagrama de Fluxo e Arquitetura

O sistema implementa o padrão **MVVM (Model-View-ViewModel)** com **Clean Architecture**:

```
[ Usuário / Dispositivo ]
        │  ▲
        │  │ Interações de UI (Compose)
        ▼  │
 [ WeatherScreen ] ──> [ WeatherViewModel ]
                             │   ▲
                             │   │ StateFlow<WeatherUiState>
                             ▼   │
                   [ WeatherRepository ] (Single Source of Truth)
                         │           │           │
         ┌───────────────┴──┐        │           └────────────────┐
         ▼                  ▼        ▼                            ▼
[ Open-Meteo API ]  [ Room DB ]  [ Gemini API ]          [ LocationHelper ]
 (Previsão & Busca)  (Favoritos) (Assistente IA)          (FusedLocation)
```

### Componentes de Dados:
- **`WeatherRepository`**: Orquestra chamadas assíncronas em `Dispatchers.IO`, garantindo que a interface nunca sofra travamentos (ANR).
- **`WeatherViewModel`**: Centraliza o estado reativo da aplicação em `StateFlow<WeatherUiState>`.
- **`SavedCityDao`**: Interface de acesso a dados Room para armazenar cidades salvas pelo usuário com persistência SQLite.
- **`GeminiService`**: Comunicação com os modelos generativos da Google para inferência em linguagem natural com fallback local.

---

## 3. Especificação das APIs Integradas

### 3.1 Open-Meteo Weather API
- **Endpoint Base**: `https://api.open-meteo.com/v1/`
- **Funcionalidade**: Obtenção de previsões meteorológicas atuais, horárias e diárias com base em latitude e longitude.
- **Parâmetros Utilizados**:
  - `current_weather=true`: Temperatura atual, velocidade e direção do vento, código WMO e indicador dia/noite.
  - `hourly=temperature_2m,relativehumidity_2m,weathercode,precipitation_probability`: Previsão de 24h a 48h.
  - `daily=weathercode,temperature_2m_max,temperature_2m_min,apparent_temperature_max,precipitation_probability_max,uv_index_max,sunrise,sunset`: Previsão para os próximos 7 dias.

### 3.2 Open-Meteo Geocoding API
- **Endpoint Base**: `https://geocoding-api.open-meteo.com/v1/`
- **Funcionalidade**: Pesquisa e autocompletação de cidades globais com coordenadas geográficas, estado e país no idioma solicitado (`pt`, `es`, `en`).

### 3.3 Google Gemini API
- **Modelo**: `gemini-3.5-flash`
- **Endpoint Base**: `https://generativelanguage.googleapis.com/v1beta/models/`
- **Configuração de Persona**:
  - Instruções de sistema orientadas para um tom caloroso, prestativo e humano.
  - Contextualização dinâmica injetando os dados meteorológicos atuais da cidade ativa.
  - Fallback local autônomo caso o dispositivo esteja offline ou sem chave configurada.

---

## 4. Estrutura de Modelagem de Dados

### 4.1 Entidades de Banco de Dados (`SavedCityEntity`)
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `Long (PK Auto)` | Identificador único no SQLite |
| `name` | `String` | Nome da cidade (ex: "São Paulo") |
| `state` | `String?` | Estado / Província |
| `country` | `String?` | País correspondente |
| `latitude` | `Double` | Coordenada de latitude |
| `longitude` | `Double` | Coordenada de longitude |
| `isFavorite` | `Boolean` | Indicador de cidade favoritada |

### 4.2 Estado de UI (`WeatherUiState`)
O estado de tela é imutável e gerenciado atomicamente através de `copy()`:
```kotlin
data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val weatherData: WeatherDetails? = null,
    val error: String? = null,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<GeocodingResultDto> = emptyList(),
    val chatMessages: List<ChatMessage> = ...,
    val isAiLoading: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val selectedLanguage: AppLanguage = AppLanguage.PT
)
```

---

## 5. Mapeamento de Códigos Meteorológicos (WMO)

A Organização Meteorológica Mundial categoriza as condições climáticas através de códigos inteiros, mapeados pelo `WeatherType.kt`:

| Código WMO | Condição (PT) | Condición (ES) | Condition (EN) | Ícone |
| :---: | :--- | :--- | :--- | :---: |
| `0` | Céu Limpo / Noite Estrelada | Cielo Despejado | Clear Sky / Night | ☀️ / 🌙 |
| `1, 2` | Poucas Nuvens / Parcialmente Nublado | Mayormente Despejado | Mostly Clear / Partly Cloudy | ⛅ |
| `3` | Nublado | Nublado | Overcast | ☁️ |
| `45, 48` | Nevoeiro / Neblina | Niebla / Neblina | Fog / Mist | 🌫️ |
| `51, 53, 55` | Garoa Fina | Llovizna Fina | Light Drizzle | 🌦️ |
| `61, 63, 65` | Chuva Leve / Moderada / Forte | Lluvia Ligera / Moderada / Fuerte | Light / Moderate / Heavy Rain | 🌧️ |
| `71, 73, 75` | Precipitação de Neve | Nevada | Snowfall | ❄️ |
| `80, 81, 82` | Pancadas de Chuva | Chubascos | Rain Showers | 🌧️ |
| `95, 96, 99` | Tempestade com Raios / Granizo | Tormenta Eléctrica / Granizo | Thunderstorm | ⛈️ |

---

## 6. Segurança e Boas Práticas

- **Permissões em Tempo de Execução**: As permissões `ACCESS_FINE_LOCATION` e `ACCESS_COARSE_LOCATION` são solicitadas com tratamento gracioso de recusa, permitindo que o usuário utilize a busca manual sem bloqueios.
- **Gerenciamento de Segredos**: A chave do Gemini é lida através do `BuildConfig.GEMINI_API_KEY` injetado pelo arquivo `.env`, impedindo vazamento de credenciais em repositórios públicos.
- **Resiliência Offline**: Se as chamadas à IA falharem ou o dispositivo estiver sem internet, o aplicativo utiliza o motor de heurística local (`generateLocalHumanAdvice`), mantendo a experiência do usuário intacta.
- **Eficiência de Rede**: Debounce de 350ms na busca de cidades para evitar sobrecarga de requisições enquanto o usuário digita.

---

## 7. Manutenção e Próximos Passos

Para equipes de desenvolvimento futuras:
- Para adicionar novos idiomas, basta estender o enum `AppLanguage` e incluir as correspondências na classe `WeatherLocalization`.
- Para adicionar novos tipos de alertas ou integrações com sensores de pressão barométrica do dispositivo, basta registrar o listener no `LocationHelper` ou em um novo `SensorHelper`.

---
*Documentação atualizada e validada para a versão 1.0.0 do Clima Tempo.*
