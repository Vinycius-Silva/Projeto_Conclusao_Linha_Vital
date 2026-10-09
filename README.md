# Linha Vital

MVP Android + Spring Boot voltado a check-ins preventivos, detecção de inatividade, rede de contatos de emergência e acionamento manual de SOS.

O objetivo desta versão é manter um fluxo demonstrável e coerente para o TCC, separando claramente o que já funciona no produto do que permanece como evolução. O MVP atual combina autenticação, monitoramento por check-in, alertas automáticos por e-mail, localização pontual e uma cascata assistida de ligações no SOS manual.

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
        +-> alerta de inatividade
        +-> e-mail automático aos contatos
        +-> última localização, quando disponível

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
- lembrete local de check-in via `AlarmManager` + notificação Android;
- registro de check-in no backend;
- scheduler no backend para detectar o vencimento do intervalo de inatividade;
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
Android (Kotlin + XML/ViewBinding)
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
```

### Monitoramento

```text
GET  /monitoramento/status/{usuarioId}
PUT  /monitoramento/configuracao/{usuarioId}
POST /monitoramento/check-in/{usuarioId}
POST /monitoramento/atividade/{usuarioId}
```

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

Há ainda endpoints de histórico de notificações e módulos auxiliares usados pelo backend.

## Pré-requisitos

### Backend

- JDK 17;
- PostgreSQL;
- servidor SMTP válido para o envio automático de e-mails de inatividade;
- credenciais OAuth do Google se o login Google for utilizado;
- acesso às dependências Gradle/Maven na primeira execução.

### Android

- Android Studio compatível com o Android Gradle Plugin utilizado no projeto;
- SDK Android configurado;
- emulador ou dispositivo Android com API 26+;
- Google Play Services para login Google e localização;
- Web Client ID do Google se o login Google for utilizado.

## Configuração do backend

O backend não deve conter credenciais reais versionadas. As propriedades principais do banco e da aplicação são obtidas do ambiente.

`Source/backend/.env.example` contém apenas a configuração básica. Como este ajuste atualiza somente a documentação, use também a lista abaixo como referência para as variáveis atualmente lidas pelo código.

### Banco e JPA

```text
DB_URL=jdbc:postgresql://localhost:5432/linhavital
DB_USER=linhavital
DB_PASSWORD=troque_esta_senha
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

```text
MONITORAMENTO_SCHEDULER_MS=5000
```

Esse valor controla o intervalo entre as verificações do scheduler do backend e não deve ser confundido com o intervalo de check-in configurado pelo usuário.

### E-mail de inatividade

O serviço usa Spring Mail. Configure um servidor SMTP compatível, por exemplo por meio das propriedades de ambiente do Spring Boot:

```text
SPRING_MAIL_HOST=smtp.exemplo.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=<usuario-smtp>
SPRING_MAIL_PASSWORD=<senha-smtp>
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

LINHA_VITAL_EMAIL_REMETENTE=<email-remetente>
EMAIL_MAX_TENTATIVAS=3
EMAIL_INTERVALO_TENTATIVAS_MS=2000
```

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
$env:DB_PASSWORD="sua_senha"

$env:GOOGLE_OAUTH_CLIENT_ID="seu-web-client-id"

$env:SPRING_MAIL_HOST="smtp.exemplo.com"
$env:SPRING_MAIL_PORT="587"
$env:SPRING_MAIL_USERNAME="usuario"
$env:SPRING_MAIL_PASSWORD="senha"
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH="true"
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE="true"
$env:LINHA_VITAL_EMAIL_REMETENTE="linha-vital@exemplo.com"

cd Source\backend
.\gradlew.bat bootRun
```

## Configuração do Android

O módulo Android recebe duas propriedades principais do Gradle:

```text
API_BASE_URL
GOOGLE_WEB_CLIENT_ID
```

O `app/build.gradle.kts` utiliza `http://10.0.2.2:8080/` como fallback para `API_BASE_URL`, mas um valor definido em `Source/frontend/gradle.properties` ou pela linha de comando sobrescreve esse padrão.

Exemplo para emulador local:

```powershell
cd Source\frontend
.\gradlew.bat assembleDebug `
  -PAPI_BASE_URL=http://10.0.2.2:8080/ `
  -PGOOGLE_WEB_CLIENT_ID=seu-web-client-id
```

Para dispositivo físico, use um endereço acessível pelo aparelho.

A configuração atual permite HTTP apenas em contextos explicitamente liberados para desenvolvimento/homologação. Uma distribuição pública deve utilizar HTTPS e remover exceções de cleartext que não sejam necessárias.

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

Ao consultar o status pela primeira vez, o backend cria uma configuração padrão de monitoramento para o usuário.

O ciclo básico é:

```text
monitoramento ativo
        |
        v
intervalo configurado vence
        |
        v
backend detecta inatividade
        |
        +-> cria um alerta de INATIVIDADE
        +-> marca a ocorrência como aberta
        +-> registra o evento
        +-> envia e-mail aos contatos
        +-> inclui a última localização, quando disponível
```

Cada check-in:

1. atualiza a última confirmação;
2. encerra alertas de inatividade ativos;
3. reinicia o ciclo preventivo;
4. permite ao Android agendar o próximo lembrete local.

Enquanto `alertaInatividadeAberto` estiver ativo, o scheduler não cria uma nova ocorrência para o mesmo ciclo.

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

- a infraestrutura de desenvolvimento/homologação ainda pode utilizar HTTP em hosts explicitamente liberados; produção deve usar HTTPS;
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
12. configurar o check-in para um intervalo curto de demonstração;
13. confirmar `Estou bem` e verificar atualização do ciclo;
14. aguardar uma inatividade e confirmar a criação de apenas um alerta aberto;
15. validar o envio de e-mail aos contatos;
16. conceder localização e verificar que a última posição é persistida;
17. repetir o fluxo sem permissão de localização e confirmar que o restante continua funcionando;
18. acionar o SOS;
19. validar a tentativa de ligação em cascata e o registro de `ATENDIDO`/`NAO_ATENDIDO`;
20. validar comportamento com API indisponível e com token expirado/revogado.

## Testes automatizados

A cobertura automatizada ainda é limitada. O backend possui testes do serviço de senhas e teste de contexto; o Android mantém cobertura mínima. Antes de tratar requisitos de desempenho, disponibilidade, bateria e confiabilidade como comprovados, é necessário executar e registrar testes específicos para esses critérios.

## Observação sobre credenciais

Se uma senha, Client Secret, credencial SMTP, chave Firebase ou outro segredo real já tiver sido commitado ou enviado para um repositório remoto, remover o valor do arquivo atual não é suficiente. O segredo deve ser rotacionado e, quando necessário, removido do histórico do repositório.
