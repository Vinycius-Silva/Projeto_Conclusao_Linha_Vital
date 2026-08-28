# Projeto_Conclusao_Linha_Vital

Aplicativo mobile de monitoramento preventivo e acionamento de contatos em situações de possível emergência.

O Linha Vital foi desenvolvido como projeto de conclusão de curso/extensão em Análise e Desenvolvimento de Sistemas. A proposta é oferecer uma solução acessível para pessoas que vivem sozinhas ou que necessitam de uma camada adicional de segurança, utilizando o smartphone como principal dispositivo.

## 1. Objetivo do projeto

O aplicativo busca:

- monitorar indiretamente a atividade do usuário;
- permitir o cadastro de contatos de emergência;
- oferecer acionamento manual de emergência;
- registrar alertas de pânico;
- realizar chamadas para contatos cadastrados;
- manter uma estrutura preparada para notificações e mecanismos de monitoramento preventivo;
- utilizar recursos do próprio smartphone, reduzindo a necessidade de hardware dedicado.

O projeto foi concebido com foco doméstico e social, especialmente para idosos, pessoas com necessidades especiais, pessoas em tratamento contínuo e indivíduos que podem permanecer longos períodos sem supervisão.

## 2. Estado atual do projeto

A implementação atual possui principalmente a camada mobile Android.

Entre os componentes já presentes estão:

- autenticação de usuário;
- cadastro;
- gerenciamento de sessão;
- tela principal;
- cadastro, listagem e exclusão de contatos;
- acionamento manual de emergência;
- registro de alerta de pânico;
- chamada telefônica para contato cadastrado;
- comunicação com API REST;
- modelos e repositórios para usuários, contatos e alertas.

A aplicação utiliza XML/View Binding nas telas principais e também mantém componentes de Jetpack Compose no projeto. Portanto, a estrutura atual é híbrida, embora o fluxo principal do aplicativo utilize Activities com layouts XML.

## 3. Tecnologias

### Android

- Kotlin
- Android SDK
- AndroidX
- Material Components
- View Binding
- XML Layouts
- Jetpack Compose
- ViewModel
- LiveData
- Coroutines
- DataStore Preferences

### Comunicação

- Retrofit
- Gson Converter
- OkHttp
- Logging Interceptor
- API REST

### Navegação e interface

- AppCompat
- ConstraintLayout
- Navigation Component
- RecyclerView
- Material Components

### Testes

- JUnit
- AndroidX Test
- Espresso

## 4. Requisitos de plataforma

Configuração atual do módulo Android:

- `minSdk = 26`
- `targetSdk = 36`
- `compileSdk = 36.1`
- Java 11
- Kotlin
- Gradle Kotlin DSL

O identificador da aplicação é:

```text
com.linhavital.app
```

## 5. Estrutura principal

A camada Android está localizada em:

```text
Source/frontend/
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── java/com/linhavital/app/
        │   │   ├── MainActivity.kt
        │   │   ├── data/
        │   │   │   ├── api/
        │   │   │   ├── model/
        │   │   │   └── repository/
        │   │   ├── ui/
        │   │   │   ├── auth/
        │   │   │   ├── home/
        │   │   │   └── theme/
        │   │   └── utils/
        │   └── res/
        │       ├── layout/
        │       ├── mipmap-*/
        │       └── xml/
        └── test/
```

## 6. Principais componentes

### `ui/auth`

Contém o fluxo de autenticação:

```text
ui/auth/
├── LoginActivity.kt
├── LoginViewModel.kt
├── RegisterActivity.kt
└── RegisterViewModel.kt
```

### `ui/home`

Contém as principais telas relacionadas ao uso do aplicativo:

```text
ui/home/
├── HomeActivity.kt
├── ContatosActivity.kt
├── ContatoAdapter.kt
└── ContatoViewModel.kt
```

### `data`

Organiza comunicação e persistência relacionada ao domínio:

```text
data/
├── api/
│   ├── ApiClient.kt
│   ├── ApiConfig.kt
│   └── ApiService.kt
├── model/
│   ├── ContatoEmergencia.kt
│   └── Usuario.kt
└── repository/
    ├── AlertaRepository.kt
    ├── ContatoRepository.kt
    └── UsuarioRepository.kt
```

### `utils`

Contém recursos auxiliares, incluindo gerenciamento de sessão e adaptação da interface às áreas do sistema Android:

```text
utils/
├── SessionManager.kt
└── WindowInsets.kt
```

## 7. Correção de layout responsivo

Uma das alterações realizadas nesta versão foi a correção da adaptação das telas a diferentes dispositivos Android.

### Problema identificado

Alguns elementos da interface, principalmente botões e a navegação inferior, podiam ficar parcialmente ocultos ou sobrepostos pela barra de navegação do Android ou pela área reservada aos gestos do sistema.

Isso ocorria porque determinados elementos do aplicativo eram dimensionados sem considerar dinamicamente as áreas ocupadas pelo sistema operacional.

O problema era particularmente relevante na tela principal, que possui uma navegação inferior própria, além de um botão SOS de tamanho fixo.

### Solução adotada

A solução não consiste em esconder a barra de navegação do aparelho.

O aplicativo passou a considerar as áreas reservadas pelo sistema por meio de `WindowInsetsCompat`.

O comportamento adotado é:

```text
┌─────────────────────────────┐
│ Barra de status do Android  │
├─────────────────────────────┤
│                             │
│ Conteúdo do Linha Vital     │
│                             │
│             SOS             │
│                             │
├─────────────────────────────┤
│ Navegação do aplicativo     │
├─────────────────────────────┤
│ Área de gestos/navegação    │
│ do Android                  │
└─────────────────────────────┘
```

A navegação nativa continua disponível para o usuário, enquanto os elementos interativos do aplicativo permanecem dentro da área utilizável.

### Arquivo responsável

Foi criado:

```text
Source/frontend/app/src/main/java/com/linhavital/app/utils/WindowInsets.kt
```

Esse arquivo fornece a função:

```kotlin
applySystemBarsPadding()
```

Ela considera:

- barra de status;
- barra de navegação;
- área de gestos;
- padding original do componente.

O maior valor entre a área inferior da barra de sistema e a área inferior de gestos é utilizado para evitar que os componentes fiquem sob os controles do sistema.

## 8. Telas modificadas na correção responsiva

### Home

Arquivos:

```text
Source/frontend/app/src/main/java/com/linhavital/app/ui/home/HomeActivity.kt
Source/frontend/app/src/main/res/layout/activity_home.xml
```

Alterações:

- uso de edge-to-edge;
- aplicação de inset superior ao cabeçalho;
- aplicação de inset inferior ao container da navegação;
- separação entre a altura visual da navegação do aplicativo e a área ocupada pelo sistema.

### Login

Arquivos:

```text
Source/frontend/app/src/main/java/com/linhavital/app/ui/auth/LoginActivity.kt
Source/frontend/app/src/main/res/layout/activity_login.xml
```

Alterações:

- uso de edge-to-edge;
- aplicação dinâmica dos insets ao conteúdo;
- preservação do espaço necessário na parte inferior;
- suporte melhorado a telas com menor altura.

### Cadastro

Arquivos:

```text
Source/frontend/app/src/main/java/com/linhavital/app/ui/auth/RegisterActivity.kt
Source/frontend/app/src/main/res/layout/activity_register.xml
```

Alterações equivalentes às realizadas no Login.

### Contatos

Arquivos:

```text
Source/frontend/app/src/main/java/com/linhavital/app/ui/home/ContatosActivity.kt
Source/frontend/app/src/main/res/layout/activity_contatos.xml
```

Alterações:

- aplicação de inset superior ao cabeçalho;
- aplicação de inset inferior ao conteúdo;
- preservação da área utilizável do `RecyclerView`.

## 9. Teclado virtual

As Activities de autenticação foram configuradas com:

```xml
android:windowSoftInputMode="adjustResize"
```

nos seguintes componentes:

```text
Source/frontend/app/src/main/AndroidManifest.xml
```

Isso permite que a área disponível para Login e Cadastro seja redimensionada quando o teclado virtual aparecer.

A medida é importante porque o teclado pode reduzir significativamente a altura disponível em aparelhos menores.

## 10. Edge-to-edge

As telas XML modificadas utilizam:

```kotlin
WindowCompat.setDecorFitsSystemWindows(window, false)
```

O objetivo é permitir que a aplicação trabalhe corretamente com as áreas do sistema e, ao mesmo tempo, controle quais componentes devem receber os respectivos insets.

O princípio utilizado é:

> A aplicação pode ocupar a área da janela, mas seus elementos interativos não devem ficar escondidos atrás das áreas controladas pelo sistema operacional.

## 11. Permissões utilizadas

O `AndroidManifest.xml` atualmente declara permissões relacionadas às funcionalidades previstas no projeto, incluindo:

```text
INTERNET
ACCESS_NETWORK_STATE
POST_NOTIFICATIONS
FOREGROUND_SERVICE
FOREGROUND_SERVICE_HEALTH
FOREGROUND_SERVICE_CONNECTED_DEVICE
WAKE_LOCK
VIBRATE
RECEIVE_BOOT_COMPLETED
CALL_PHONE
```

A permissão `CALL_PHONE` é utilizada para o acionamento telefônico relacionado ao fluxo de emergência.

## 12. Fluxo básico da aplicação

O fluxo principal atualmente é:

```text
Login
  │
  ├── Cadastro
  │
  └── Autenticação
        │
        ▼
      Home
        │
        ├── Contatos
        │     ├── Listar contatos
        │     ├── Cadastrar contato
        │     └── Excluir contato
        │
        ├── SOS
        │     ├── Registrar alerta
        │     └── Ligar para contato
        │
        └── Logout
```

O acionamento manual de emergência também possui suporte a interação física configurada na `HomeActivity`.

## 13. Configuração do projeto

O projeto Android está em:

```text
Source/frontend/
```

Para abrir o projeto:

1. Abra o diretório `Source/frontend` no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Verifique se o SDK Android necessário está instalado.
4. Execute a configuração `app`.
5. Conecte um dispositivo Android ou utilize um emulador.
6. Execute o aplicativo.

## 14. Backend

A camada mobile possui clientes e repositórios preparados para comunicação com uma API REST:

```text
Source/frontend/app/src/main/java/com/linhavital/app/data/api/
```

Os componentes principais são:

```text
ApiConfig.kt
ApiClient.kt
ApiService.kt
```

As URLs e configurações específicas da API devem ser verificadas antes da execução em ambiente de produção.

## 15. Testes de responsividade

A correção de layout deve ser validada em diferentes condições, especialmente:

| Cenário | Resultado esperado |
|---|---|
| Android com barra de navegação por botões | Conteúdo não fica atrás da barra |
| Android com navegação por gestos | Conteúdo respeita a área de gestos |
| Tela pequena | Botões permanecem acessíveis |
| Tela grande | Conteúdo continua organizado |
| Teclado aberto | Campos e botões continuam acessíveis |
| Fonte aumentada | Interface continua utilizável |
| Orientação paisagem | Elementos não são cortados |

É importante testar tanto dispositivos físicos quanto emuladores com diferentes resoluções e modos de navegação.

## 16. Validação desta versão

Os arquivos XML modificados foram verificados quanto à estrutura sintática.

A compilação completa do projeto não foi concluída no ambiente utilizado para esta correção porque o Gradle Wrapper precisou obter o Gradle 9.3.1 a partir de `services.gradle.org`, enquanto o ambiente de execução não possuía acesso de rede.

Portanto, esta versão **não deve ser considerada como uma build compilada e validada em dispositivo** até que a equipe execute o projeto localmente.

## 17. Arquivos relacionados à correção

Além deste README, há um documento específico descrevendo as alterações:

```text
CORRECAO_LAYOUT_RESPONSIVO.md
```

Os principais arquivos modificados nessa correção são:

```text
Source/frontend/app/src/main/java/com/linhavital/app/utils/WindowInsets.kt

Source/frontend/app/src/main/java/com/linhavital/app/ui/home/HomeActivity.kt
Source/frontend/app/src/main/java/com/linhavital/app/ui/home/ContatosActivity.kt

Source/frontend/app/src/main/java/com/linhavital/app/ui/auth/LoginActivity.kt
Source/frontend/app/src/main/java/com/linhavital/app/ui/auth/RegisterActivity.kt

Source/frontend/app/src/main/res/layout/activity_home.xml
Source/frontend/app/src/main/res/layout/activity_login.xml
Source/frontend/app/src/main/res/layout/activity_register.xml
Source/frontend/app/src/main/res/layout/activity_contatos.xml

Source/frontend/app/src/main/AndroidManifest.xml
```

## 18. Próximos pontos recomendados

A correção de insets resolve a sobreposição causada pelas áreas do sistema, mas a responsividade do aplicativo não deve depender somente dela.

Como próximos passos, recomenda-se:

- testar as quatro telas em aparelhos com diferentes proporções de tela;
- testar navegação por gestos e por três botões;
- testar tamanhos de fonte maiores;
- verificar comportamento em orientação paisagem;
- revisar dimensões fixas, especialmente elementos grandes da Home;
- verificar se textos longos podem quebrar o layout;
- revisar acessibilidade dos elementos interativos;
- adicionar testes instrumentados para os principais cenários de responsividade;
- realizar uma compilação e instalação local antes de considerar a alteração concluída.

## 19. Contexto do projeto

O Linha Vital foi concebido como uma solução de monitoramento preventivo e acionamento de emergência. A proposta utiliza o smartphone do usuário como principal dispositivo e busca reduzir o tempo de resposta em situações nas quais uma pessoa possa permanecer sem assistência.

A solução prevê camadas de proteção preventiva, reativa automática e reativa manual, além do cadastro de contatos de confiança e emergência.

