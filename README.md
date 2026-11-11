# Linha Vital

MVP Android + Spring Boot voltado a check-ins preventivos, detecção de inatividade, rede de contatos de emergência e acionamento manual de SOS.

O Linha Vital é um projeto acadêmico desenvolvido no contexto do Trabalho de Conclusão de Curso (TCC) de Análise e Desenvolvimento de Sistemas do Instituto Federal de São Paulo (IFSP). Seu objetivo é oferecer um protótipo de acompanhamento preventivo que permita ao usuário confirmar periodicamente seu bem-estar e recorrer a contatos cadastrados em situações de inatividade ou no acionamento manual de SOS.

O objetivo desta versão é manter um fluxo demonstrável e coerente para o TCC, separando claramente o que já funciona no produto do que permanece como evolução. O MVP atual combina autenticação, monitoramento por check-in, alertas automáticos por e-mail, localização pontual e uma cascata assistida de ligações no SOS manual.

> **Limite de uso:** este MVP não substitui serviços de emergência, acompanhamento médico ou sistemas profissionais de proteção. O envio de alertas e as ligações dependem de conectividade, permissões e disponibilidade dos serviços envolvidos; não há garantias formais de disponibilidade ou tempo de resposta.

## Estado atual do MVP

Fluxo principal:

```text
Cadastro / Login (e-mail e senha ou Google)
                    |
                    v
               Onboarding
                    |
                    v
                   Home
        +-----------+------------+
        |           |            |
        v           v            v
     Check-in    Contatos      Critérios
     "Estou bem"  CRUD +       intervalo /
                  prioridade    ativo-pausado
        |
        v
Monitoramento de inatividade no backend
        |
        +-> fim do intervalo: check-in fica pendente
        +-> período de tolerância (padrão: 15 min)
        +-> sem novo check-in: abre alerta de inatividade
        +-> tenta enviar e-mail aos contatos
        +-> inclui última localização, quando disponível

SOS manual
   |
   +-> registra alerta de pânico
   +-> carrega contatos por prioridade
   +-> tenta ligação em cascata
   +-> registra o resultado informado pelo usuário
```

### Implementado nesta versão

- cadastro tradicional com nome, e-mail, telefone e data de nascimento;
- login por e-mail e senha via `POST /auth/login`;
- login com Conta Google usando Credential Manager no Android e validação do ID Token no backend;
- complementação cadastral quando uma Conta Google ainda não possui todos os dados necessários ao Linha Vital;
- senhas armazenadas com PBKDF2-HMAC-SHA256 e salt individual;
- migração de senhas legadas em texto puro após autenticação bem-sucedida;
- sessões stateless baseadas em token Bearer opaco, com expiração e revogação no logout;
- armazenamento apenas do hash SHA-256 do token de sessão no backend;
- envio automático do Bearer Token pelo cliente Android nas requisições autenticadas;
- validação de propriedade nos principais recursos associados ao usuário;
- sessão local persistida com DataStore;
- onboarding em quatro etapas antes do primeiro uso;
- Home com estado do monitoramento, próximo check-in e ação `Estou bem`;
- configuração do monitoramento preventivo (`ativo`/`pausado` + intervalo entre 1 e 240 minutos);
- exigência de pelo menos um contato de emergência cadastrado para ativar o monitoramento;
- lembrete local de check-in via `AlarmManager` + notificação Android;
- registro de check-in no backend;
- indicação de check-in pendente ao vencer o intervalo configurado;
- tolerância adicional configurável no backend (padrão: 15 minutos) antes do alerta aos contatos;
- scheduler no backend para identificar os ciclos que ultrapassaram intervalo e tolerância;
- apenas um alerta de inatividade aberto por ciclo, evitando duplicação contínua;
- resolução do alerta de inatividade após novo check-in ou alteração da configuração;
- envio automático de e-mail aos contatos quando uma inatividade é detectada;
- repetição configurável do envio de e-mail em caso de erro;
- registro do resultado das tentativas de notificação;
- cadastro, edição, exclusão, listagem e ligação para contatos de emergência;
- contatos com nome, telefone, e-mail, tipo e prioridade de acionamento;
- captura pontual da localização atual, mediante permissão do usuário;
- envio da localização ao backend e inclusão da última posição conhecida no alerta por e-mail, quando disponível;
- SOS manual acionável por três interações na Home;
- atalho por volume para baixo enquanto a `HomeActivity` está em primeiro plano;
- criação de alerta de pânico no backend;
- cascata assistida de ligações conforme a prioridade dos contatos;
- registro de tentativa como `ATENDIDO` ou `NAO_ATENDIDO` conforme confirmação do usuário;
- configuração sensível do backend baseada em variáveis de ambiente;
- Firebase Admin mantido como integração opcional e desabilitado por padrão;
- logging HTTP reduzido em `debug` e desabilitado fora de `debug`.

## Fora do MVP atual

Os itens abaixo permanecem como evolução do projeto e não devem ser apresentados como funcionalidades concluídas:

- detecção automática de quedas;
- monitoramento contínuo de uso, movimento ou desbloqueio do smartphone;
- localização contínua em segundo plano;
- SOS global por botão físico com o aplicativo encerrado ou fora de primeiro plano;
- detecção automática de que uma ligação foi atendida;
- confirmação de recebimento diretamente pelo contato de emergência;
- envio automático de SMS;
- fluxo FCM completo para contatos externos;
- integração automática com SAMU, bombeiros, polícia ou outros serviços públicos;
- wearables e smartwatch;
- garantias de disponibilidade, consumo de bateria ou tempo de resposta ainda não validadas por ensaios formais.

## Arquitetura

```text
Android (Kotlin + XML/ViewBinding + componentes Compose)
        |
        | Retrofit / JSON
        | Authorization: Bearer <token>
        v
Spring Boot (Kotlin + Spring Security)
        |
        +-> PostgreSQL
        |
        +-> SMTP / Spring Mail
        |
        +-> Google OAuth Token Validation
        |
        +-> Firebase Admin (opcional)
```

### Android

O aplicativo é responsável pelas telas, autenticação do usuário, lembretes locais, confirmação de check-in, coleta pontual de localização (com permissão) e orientação da cascata de ligações do SOS. A comunicação com a API usa Retrofit/OkHttp e JSON.

Código principal:

```text
Source/frontend/app/src/main/java/com/linhavital/app/
├── data/
│   ├── api/
│   ├── model/
│   └── repository/
├── location/
├── monitoring/
│   ├── CheckInReminderReceiver.kt
│   └── CheckInScheduler.kt
├── ui/
│   ├── auth/
│   ├── home/
│   └── onboarding/
└── utils/
```

Telas/estados principais:

- Login;
- Cadastro;
- complemento cadastral do login Google;
- Onboarding;
- Home;
- Critérios;
- Configurar critério;
- Contatos;
- Adicionar/editar contato.

### Backend

A API concentra a autenticação e a autorização, as regras de negócio, a persistência no PostgreSQL, o processamento agendado de inatividade e as tentativas de notificação por e-mail. Spring Security protege as rotas autenticadas; Firebase Admin permanece como integração opcional.

Código principal:

```text
Source/backend/src/main/kotlin/com/linhavital/backend/
├── config/
├── controller/
├── dto/
├── exception/
├── model/
├── repository/
├── security/
└── service/
```

O backend utiliza Spring Security com política stateless. Apenas os endpoints necessários para autenticação e cadastro inicial são públicos; os demais fluxos principais exigem sessão autenticada.

## Principais endpoints

### Autenticação

```text
POST /auth/login
POST /auth/google
POST /auth/google/cadastro
GET  /auth/me
POST /auth/logout
```

### Usuário

```text
POST   /usuarios
GET    /usuarios/{id}
PUT    /usuarios/{id}
DELETE /usuarios/{id}
```

### Contatos

```text
GET    /contatos/usuario/{usuarioId}
POST   /contatos/usuario/{usuarioId}
PUT    /contatos/usuario/{usuarioId}/{contatoId}
DELETE /contatos/usuario/{usuarioId}/{contatoId}
PUT    /contatos/usuario/{usuarioId}/ordem
```

### Monitoramento

```text
GET  /monitoramento/status/{usuarioId}
PUT  /monitoramento/configuracao/{usuarioId}
POST /monitoramento/check-in/{usuarioId}
POST /monitoramento/atividade/{usuarioId}
```

**Importante:** o endpoint `POST /monitoramento/atividade/{usuarioId}` atualmente utiliza a mesma lógica de um check-in manual. Ele **não** indica detecção passiva de movimento, desbloqueio ou uso do smartphone.

### Alertas

```text
POST /alerta/panico/{usuarioId}
GET  /alerta/usuario/{usuarioId}
```

### Localização

```text
GET  /localizacoes
GET  /localizacoes/ultima
POST /localizacoes
```

### Histórico de notificações

```text
GET  /notificacoes
POST /notificacoes
POST /notificacoes/alerta/{alertaId}/tentativa
```

Há ainda endpoints de dispositivos e módulos auxiliares usados pelo backend. As rotas protegidas exigem token Bearer válido e, quando aplicável, a verificação de acesso ao recurso do usuário.

## Pré-requisitos

### Backend

- JDK 17 (versão definida no toolchain Java do backend);
- PostgreSQL instalado e acessível;
- servidor SMTP válido para o envio automático de e-mails de inatividade;
- credenciais OAuth do Google se o login Google for utilizado;
- acesso às dependências Gradle/Maven na primeira execução.

### Android

- Android Studio compatível com o Android Gradle Plugin e Gradle Wrapper do projeto;
- SDK Android configurado (o projeto usa `compileSdk` 36.1, `targetSdk` 36 e `minSdk` 26);
- emulador ou dispositivo Android com API 26+;
- Google Play Services para login Google e localização;
- Web Client ID do Google se o login Google for utilizado.

## Preparação e execução local

1. Clone ou extraia o repositório e abra um terminal **na pasta raiz**, onde ficam `Source/backend` e `Source/frontend`.
2. Instale o JDK 17 e o PostgreSQL. O projeto já inclui os Gradle Wrappers, portanto não é obrigatório instalar o Gradle globalmente.
3. Crie um banco e um usuário próprios para o projeto. Por exemplo, conectado ao PostgreSQL com uma conta administradora:

   ```sql
   CREATE USER linhavital WITH PASSWORD 'SENHA_FORTE_AQUI';
   CREATE DATABASE linhavital OWNER linhavital;
   ```

   Se já houver usuário ou banco com esses nomes, adapte as instruções em vez de recriá-los. Os exemplos de senha neste documento **não** devem ser usados literalmente em uma implantação.

4. Defina as variáveis de ambiente do backend conforme as seções seguintes e execute `Source/backend/gradlew.bat bootRun` a partir da pasta `Source/backend` (exemplo completo em PowerShell abaixo).
5. Configure o endereço da API no Android, abra `Source/frontend` no Android Studio e instale o aplicativo em um emulador ou dispositivo compatível.

## Configuração do backend

O backend não deve conter credenciais reais versionadas. As propriedades principais do banco e da aplicação são obtidas do ambiente.

`Source/backend/.env.example` contém apenas a configuração básica. **O Spring Boot não importa automaticamente um arquivo `.env` apenas por ele existir na pasta.** Para execução local, defina as variáveis na sessão do terminal ou use outro mecanismo de carregamento configurado explicitamente. Como esta alteração modifica somente a documentação, nenhuma configuração do backend foi alterada.

### Banco e JPA

```text
DB_URL=jdbc:postgresql://localhost:5432/linhavital
DB_USER=linhavital
DB_PASSWORD=SENHA_FORTE_AQUI
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
```

### Sessão e autenticação Google

```text
GOOGLE_OAUTH_CLIENT_ID=<web-client-id-do-google>
AUTH_SESSION_TTL_HOURS=720
AUTH_SESSION_CLEANUP_CRON=0 0 4 * * *
```

`AUTH_SESSION_TTL_HOURS` define a validade das sessões Bearer em horas. O valor padrão do código é 720 horas.

### Monitoramento

```dotenv
INATIVIDADE_TOLERANCIA_MINUTOS=15
MONITORAMENTO_SCHEDULER_MS=5000
```

- `INATIVIDADE_TOLERANCIA_MINUTOS`: número de minutos adicionais após o vencimento do check-in antes de o backend poder abrir um alerta de inatividade. Padrão do código: **15 minutos**; valores negativos são tratados como zero.
- `MONITORAMENTO_SCHEDULER_MS`: atraso entre verificações agendadas do backend, em milissegundos. Padrão do código: **5.000 ms**.

Essas variáveis são independentes do intervalo de check-in escolhido pelo usuário (de 1 a 240 minutos). O scheduler não transforma o intervalo configurado em um limite garantido de processamento em tempo real.

### E-mail de inatividade

O serviço usa Spring Mail. Os nomes abaixo correspondem diretamente às variáveis referenciadas em `Source/backend/src/main/resources/application.yaml`:

```dotenv
MAIL_HOST=smtp.exemplo.com
MAIL_PORT=587
MAIL_USERNAME=<usuario-smtp>
MAIL_PASSWORD=<senha-smtp>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
EMAIL_REMETENTE=<email-remetente>
EMAIL_MAX_TENTATIVAS=3
EMAIL_INTERVALO_TENTATIVAS_MS=2000
```

No `application.yaml`, `EMAIL_REMETENTE` alimenta a propriedade `LINHA_VITAL_EMAIL_REMETENTE`, consumida pelo serviço de e-mail. Também é possível sobrescrever diretamente as propriedades nativas do Spring Boot (por exemplo, `SPRING_MAIL_HOST`), mas o exemplo acima segue os placeholders usados neste repositório.

O e-mail do contato precisa estar preenchido para que ele receba o alerta automático de inatividade.

### Firebase opcional

```text
FIREBASE_ENABLED=false
# GOOGLE_APPLICATION_CREDENTIALS=/caminho/absoluto/firebase-service-account.json
```

Se habilitado, o arquivo de credenciais não deve ser colocado em `src/main/resources` nem versionado.

### Exemplo de inicialização no PowerShell

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/linhavital"
$env:DB_USER="linhavital"
$env:DB_PASSWORD="SENHA_FORTE_AQUI"

$env:GOOGLE_OAUTH_CLIENT_ID="seu-web-client-id"

$env:MAIL_HOST="smtp.exemplo.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="usuario"
$env:MAIL_PASSWORD="senha"
$env:MAIL_SMTP_AUTH="true"
$env:MAIL_SMTP_STARTTLS="true"
$env:EMAIL_REMETENTE="linha-vital@exemplo.com"

$env:INATIVIDADE_TOLERANCIA_MINUTOS="15"
$env:MONITORAMENTO_SCHEDULER_MS="5000"

cd Source\backend
.\gradlew.bat bootRun
```

## Configuração do Android

O módulo Android recebe duas propriedades principais do Gradle:

```text
API_BASE_URL
GOOGLE_WEB_CLIENT_ID
```

O `Source/frontend/app/build.gradle.kts` utiliza `http://10.0.2.2:8080/` como fallback para `API_BASE_URL`, mas um valor definido em `Source/frontend/gradle.properties` ou pela linha de comando sobrescreve esse padrão. No arquivo `gradle.properties` incluído nesta versão, a propriedade `API_BASE_URL` aponta para uma VM por **HTTP**, uma configuração de protótipo que deve ser revista antes de qualquer distribuição pública.

Exemplo para emulador local:

```powershell
cd Source\frontend
.\gradlew.bat assembleDebug `
  -PAPI_BASE_URL=http://10.0.2.2:8080/ `
  -PGOOGLE_WEB_CLIENT_ID=seu-web-client-id
```

Para dispositivo físico, use um endereço acessível pelo aparelho. O endereço `10.0.2.2` é específico do emulador Android e não aponta para o computador de desenvolvimento quando usado em um telefone físico.

### Compilação e APK

Os comandos a seguir partem da pasta raiz do repositório e geram um APK de depuração:

```powershell
cd Source\frontend
.\gradlew.bat clean assembleDebug
```

O APK resultante é gerado em `Source/frontend/app/build/outputs/apk/debug/app-debug.apk`. Para testar a API local no emulador, ajuste `API_BASE_URL` antes da compilação (por parâmetro `-P` ou nas propriedades Gradle). Um APK `debug` **não** equivale a uma versão final de produção assinada e validada.

A configuração de rede em `Source/frontend/app/src/main/res/xml/network_security_config.xml` bloqueia cleartext por padrão e libera HTTP para destinos específicos de desenvolvimento/VM. **A VM atualmente configurada utiliza HTTP**, portanto a comunicação com esse destino ainda não dispõe da proteção de transporte oferecida pelo HTTPS. Antes de distribuição pública, configure TLS/HTTPS no servidor, atualize `API_BASE_URL` e elimine exceções desnecessárias de cleartext; a autenticação Bearer, isoladamente, não substitui a criptografia de transporte.

## Autenticação e sessão

### E-mail e senha

```text
Android
   |
   | POST /auth/login
   v
Backend valida usuário e senha
   |
   v
cria sessão
   |
   +-> retorna token Bearer opaco
   +-> armazena somente SHA-256 do token
   +-> define expiração
```

O Android salva os dados da sessão no DataStore e adiciona automaticamente o token às requisições autenticadas.

### Conta Google

```text
Credential Manager / Google Identity
        |
        v
ID Token Google
        |
        v
POST /auth/google
        |
        +-> valida token no backend
        +-> autentica usuário existente
        +-> ou solicita complemento cadastral
```

O Client ID utilizado pelo Android e o Client ID validado pelo backend devem corresponder à mesma configuração OAuth Web.

### Logout

`POST /auth/logout` revoga a sessão correspondente ao Bearer Token. Após isso, o cliente remove os dados locais de sessão, preservando apenas o estado de conclusão do onboarding.

## Monitoramento e check-in

Ao consultar o status pela primeira vez, o backend cria uma configuração padrão de monitoramento para o usuário. A ativação exige **pelo menos um contato de emergência** vinculado à conta.

O ciclo distingue duas situações: **check-in pendente**, quando termina o intervalo configurado pelo usuário, e **inatividade escalonada**, quando também termina o período adicional de tolerância definido no backend.

```text
monitoramento ativo e pelo menos um contato cadastrado
        |
        v
último check-in + intervalo configurado
        |
        +-> check-in passa a aparecer como PENDENTE na API/Home
        |   (ainda não há alerta nem e-mail)
        v
intervalo adicional de tolerância (padrão: 15 min)
        |
        +-> se houver novo check-in: reinicia o ciclo sem alertar
        v
se ainda não houve check-in, o scheduler pode:
        +-> criar um alerta de INATIVIDADE
        +-> marcar a ocorrência como aberta
        +-> registrar o evento no histórico
        +-> tentar enviar e-mail aos contatos
        +-> incluir a última localização, quando disponível
```

**Exemplo:** com check-in às 12h00, intervalo de 30 minutos e tolerância de 15 minutos, o check-in fica pendente às 12h30; se não houver confirmação, o processamento do alerta pode ocorrer **a partir de 12h45**, na execução seguinte do scheduler. Um novo check-in durante a tolerância cancela o escalonamento desse ciclo.

Cada check-in:

1. atualiza a última confirmação;
2. encerra alertas de inatividade ativos;
3. reinicia o ciclo preventivo;
4. permite ao Android agendar o próximo lembrete local.

Enquanto `alertaInatividadeAberto` estiver ativo, o scheduler não cria uma nova ocorrência para o mesmo ciclo. Ao pausar ou reconfigurar o monitoramento, o backend também reinicia o ciclo e resolve alertas antigos de inatividade.

O campo `checkInPendente` do status é calculado assim que o prazo do usuário termina, **antes da tolerância**; isso não equivale à confirmação de envio de e-mail. Falhas no SMTP não impedem o registro do alerta no backend, mas podem impedir o recebimento da notificação pelos contatos.

## Localização

A localização não é monitorada continuamente.

Quando o fluxo solicita uma atualização e o usuário concedeu `ACCESS_FINE_LOCATION` ou `ACCESS_COARSE_LOCATION`, o Android obtém uma posição atual por meio do Fused Location Provider e a envia ao backend.

A última localização disponível pode ser incluída nos e-mails automáticos de inatividade. Se a permissão não for concedida ou a posição não puder ser obtida, os demais fluxos continuam funcionando sem geolocalização.

## SOS manual

Na Home, o SOS pode ser iniciado por três interações na interface. Também existe um atalho por três pressões em volume para baixo enquanto a `HomeActivity` está em primeiro plano.

Fluxo atual:

```text
SOS acionado
    |
    +-> tenta atualizar a localização
    |
    +-> POST /alerta/panico/{usuarioId}
    |
    +-> busca contatos ordenados por prioridade
    |
    +-> solicita CALL_PHONE quando necessário
    |
    +-> liga para o contato atual
    |
    +-> usuário informa se houve atendimento
            |
            +-> ATENDIDO     -> encerra a cascata local
            +-> NAO_ATENDIDO -> tenta o próximo contato
```

A cascata é **assistida pelo usuário**. O aplicativo não detecta automaticamente se a ligação foi atendida.

## Segurança da versão atual

A versão atual incorpora as seguintes medidas:

- credenciais PostgreSQL fora do código-fonte;
- senhas armazenadas por derivação PBKDF2-HMAC-SHA256 com salt;
- autenticação realizada no backend;
- tokens Bearer opacos gerados com aleatoriedade criptográfica;
- somente o hash SHA-256 dos tokens é persistido;
- expiração e revogação de sessões;
- filtro de autenticação no Spring Security;
- respostas de usuário sem exposição de senha;
- validação de propriedade nos principais fluxos associados ao usuário;
- logs HTTP do Android sem corpo das requisições/respostas.

### Limitações de segurança ainda relevantes

- a configuração versionada do Android utiliza uma URL de backend em VM via **HTTP**, com liberação explícita no `network_security_config.xml`; a comunicação desse ambiente ainda não é protegida por TLS/HTTPS;
- antes de qualquer uso público, o backend deve disponibilizar HTTPS e o aplicativo deve apontar para esse endereço seguro, removendo exceções de cleartext desnecessárias;
- a autorização deve continuar sendo revisada à medida que módulos auxiliares forem incorporados ao fluxo principal;
- nenhuma chave OAuth, senha SMTP, credencial Firebase ou segredo de banco deve ser versionado.

## Funcionalidades deliberadamente não representadas como prontas

O protótipo e os documentos acadêmicos podem apresentar uma visão mais ampla do produto. Para demonstração do código atual, não trate como concluídos recursos que ainda não possuem fluxo ponta a ponta, em especial:

- queda automática;
- monitoramento passivo de uso/desbloqueio;
- SMS;
- push FCM para contatos externos;
- acionamento automático de serviços públicos;
- localização contínua;
- detecção automática de atendimento de chamada.

## Validação recomendada antes da banca

Execute o roteiro abaixo em um ambiente limpo:

1. criar o banco PostgreSQL;
2. configurar banco, sessão, SMTP e, se utilizado, OAuth Google;
3. iniciar o backend;
4. configurar `API_BASE_URL` e `GOOGLE_WEB_CLIENT_ID` no Android;
5. cadastrar um usuário por e-mail e senha;
6. testar logout e novo login;
7. testar login Google e, quando necessário, complemento cadastral;
8. percorrer o onboarding;
9. cadastrar pelo menos dois contatos com e-mail válido;
10. editar e excluir um contato;
11. verificar a ordem/prioridade dos contatos;
12. configurar o check-in para um intervalo curto de demonstração e verificar o valor de `INATIVIDADE_TOLERANCIA_MINUTOS` utilizado no ambiente;
13. confirmar `Estou bem` e verificar atualização do ciclo;
14. conferir que o check-in fica pendente ao terminar o intervalo sem notificar os contatos, e que o alerta é aberto somente após a tolerância; confirmar que não há alertas duplicados;
15. validar o envio de e-mail aos contatos;
16. conceder localização e verificar que a última posição é persistida;
17. repetir o fluxo sem permissão de localização e confirmar que o restante continua funcionando;
18. acionar o SOS;
19. validar a tentativa de ligação em cascata e o registro de `ATENDIDO`/`NAO_ATENDIDO`;
20. validar comportamento com API indisponível e com token expirado/revogado.

## Testes automatizados

O repositório contém testes automatizados locais e um teste instrumentado de exemplo. **As contagens abaixo representam métodos de teste declarados no código; não significam que a suíte foi executada ou aprovada nesta revisão.**

| Módulo | Arquivo de teste | Casos | Principais verificações |
| --- | --- | ---: | --- |
| Backend | `BackendApplicationTests.kt` | 1 | Inicialização do contexto Spring Boot. |
| Backend | `MonitoramentoServiceTests.kt` | 8 | Pré-requisito de contato, check-in pendente, tolerância, abertura de alertas, prevenção de duplicidade e monitoramento pausado. |
| Backend | `PasswordServiceTests.kt` | 2 | Derivação e validação de senha; compatibilidade com migração de senha legada. |
| Backend | `UsuarioServiceTests.kt` | 5 | Validações de nome, e-mail, telefone, senha e data de nascimento no cadastro. |
| Android (unitários locais) | `DataNascimentoUtilsTest.kt` | 8 | Conversão e validação de datas, incluindo bissextos e datas inválidas/futuras. |
| Android (unitários locais) | `SosInteractionTrackerTest.kt` | 7 | Três interações do SOS, janela de tempo, reinício e repetição da sequência. |
| Android (unitários locais) | `ExampleUnitTest.kt` | 1 | Teste básico de exemplo. |
| Android (instrumentado) | `ExampleInstrumentedTest.kt` | 1 | Contexto do aplicativo em dispositivo/emulador. |
| **Total** | | **33** | **16 backend + 16 Android locais + 1 Android instrumentado.** |

### Como executar os testes

Na pasta raiz do repositório, use um terminal PowerShell:

**Backend (JUnit + Spring Boot):**

```powershell
cd Source\backend
.\gradlew.bat clean test
```

O backend inclui o perfil de testes `Source/backend/src/test/resources/application-test.yaml`, com banco H2 em memória para os testes de contexto. Os relatórios Gradle são gerados em `Source/backend/build/reports/tests/test/index.html`, quando a tarefa é executada.

**Android — testes unitários locais:**

```powershell
cd Source\frontend
.\gradlew.bat testDebugUnitTest
```

**Android — teste instrumentado:**

```powershell
cd Source\frontend
.\gradlew.bat connectedDebugAndroidTest
```

O teste instrumentado requer emulador ou dispositivo Android conectado e configurado. Execute cada bloco a partir da **pasta raiz** (os exemplos mudam de diretório); se já estiver em `Source/backend` ou `Source/frontend`, use apenas o comando Gradle apropriado.

### Testes manuais e integrados

O roteiro da seção **Validação recomendada antes da banca** é um **checklist de verificação manual**, não uma declaração automática de aprovação. Ao registrar resultados no TCC, diferencie os cenários de cadastro/login, contatos, check-in, SOS, localização, envio de e-mail e falhas de conexão dos testes automatizados; inclua data, ambiente, resultado esperado/observado e evidências (capturas de tela e logs) quando disponíveis.

A existência desses testes **não comprova**, por si só, desempenho, disponibilidade, consumo de bateria, qualidade do serviço SMTP ou tempo de resposta em emergências. Esses aspectos exigem medições e critérios específicos.

## Documentação acadêmica

O repositório contém documentação do projeto e os fontes do TCC em LaTeX. Para evitar divergências, este README referencia a árvore localizada em `Documentação/`; como existem outras cópias da estrutura LaTeX no ZIP, recomenda-se eleger uma única versão como fonte de edição antes das próximas revisões.

- [PDF da documentação acadêmica](Documenta%C3%A7%C3%A3o/Projeto%20Integrado%20de%20Extens%C3%A3o%20ADS/TEMPLATE_TCC_IFPI%20-%20Copia%20%284%29%20-%20Copia/TEMPLATE_TCC_IFPI/documento.pdf)
- [Arquivo principal LaTeX (`documento.tex`)](Documenta%C3%A7%C3%A3o/Projeto%20Integrado%20de%20Extens%C3%A3o%20ADS/TEMPLATE_TCC_IFPI%20-%20Copia%20%284%29%20-%20Copia/TEMPLATE_TCC_IFPI/documento.tex)
- [Apresentação/proposta em PDF](Projeto/Linha%20Vital.pdf)
- [Relatório de correções da documentação](Correcoes_TCC/linha-vital-correcoes-documentacao.md)
- [Relatório de correções e feedback](Correcoes_TCC/Linha-Vital-relatorio-correcoes-feedback.md)

Ao descrever o MVP no TCC, mantenha coerência com as seções **Implementado nesta versão**, **Fora do MVP atual**, **Monitoramento e check-in** e **Limitações de segurança** deste README. Recursos planejados não devem ser apresentados como concluídos, e resultados de testes devem refletir as evidências efetivamente obtidas.

## Observação sobre credenciais

Se uma senha, Client Secret, credencial SMTP, chave Firebase ou outro segredo real já tiver sido commitado ou enviado para um repositório remoto, remover o valor do arquivo atual não é suficiente. O segredo deve ser rotacionado e, quando necessário, removido do histórico do repositório.
