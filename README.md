# Linha Vital

MVP Android + Spring Boot para check-ins preventivos, rede de contatos de emergência e acionamento manual de SOS.

O objetivo desta versão é manter um fluxo pequeno, coerente e demonstrável para o TCC. Funcionalidades que ainda não possuem implementação ponta a ponta, como detecção automática de queda e envio externo automático de SMS/FCM aos contatos, não são apresentadas como recursos ativos.

## Escopo consolidado do MVP

Fluxo principal:

```text
Cadastro -> Login -> Onboarding -> Home
                              |      |
                              |      +-> Check-in "Estou bem"
                              |      +-> SOS manual (3 toques)
                              |      +-> SOS por volume (3x, app em primeiro plano)
                              |
                              +-> Contatos -> Adicionar / Editar / Excluir / Ligar
                              |
                              +-> Critérios -> Configurar check-in por inatividade
```

### Implementado nesta versão

- cadastro com nome, e-mail, telefone e data de nascimento reais;
- login por endpoint dedicado (`POST /auth/login`), sem baixar a lista completa de usuários;
- senhas novas armazenadas com PBKDF2-HMAC-SHA256 + salt;
- migração automática de senha legada em texto puro no primeiro login bem-sucedido;
- sessão local com DataStore;
- onboarding em quatro etapas, exibido antes do primeiro uso;
- Home com estado real do monitoramento, próximo check-in e ação `Estou bem`;
- configuração do monitoramento preventivo (ativo/pausado + intervalo);
- opção de 1 minuto exclusiva para demonstração do TCC;
- lembrete local de check-in via `AlarmManager` + notificação Android;
- registro de check-in no backend;
- criação única de alerta de inatividade por ciclo, evitando duplicação a cada minuto;
- resolução do alerta de inatividade quando o usuário confirma o check-in;
- cadastro, edição, exclusão, listagem e ligação para contatos;
- prioridade determinística dos contatos;
- SOS manual com registro no backend e tentativa de ligação para o primeiro contato;
- configuração do backend por variáveis de ambiente;
- Firebase opcional e desabilitado por padrão;
- HTTP liberado somente em builds `debug`; builds normais usam política de cleartext bloqueada.

### Fora do MVP atual

Os itens abaixo permanecem como evolução do projeto e não devem ser apresentados como funcionalidades concluídas:

- detecção automática de quedas;
- monitoramento geral de uso/desbloqueio do smartphone;
- SOS global por botão físico quando o aplicativo não está em primeiro plano;
- envio automático de SMS para contatos;
- notificação FCM para contatos externos;
- integração automática com SAMU, bombeiros ou polícia;
- localização contínua;
- wearables/smartwatch.

## Arquitetura

```text
Android (Kotlin/XML/ViewBinding)
        |
        | Retrofit/JSON
        v
Spring Boot (Kotlin)
        |
        +-> PostgreSQL
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
├── monitoring/
│   ├── CheckInReminderReceiver.kt
│   └── CheckInScheduler.kt
├── ui/
│   ├── auth/
│   ├── home/
│   └── onboarding/
└── utils/
```

Telas/estados do fluxo:

- Login;
- Cadastro;
- Onboarding (4 etapas na mesma Activity);
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
├── model/
├── repository/
└── service/
```

Principais endpoints do MVP:

```text
POST /auth/login
POST /usuarios
GET  /usuarios/{id}

GET    /contatos/usuario/{usuarioId}
POST   /contatos/usuario/{usuarioId}
PUT    /contatos/usuario/{usuarioId}/{contatoId}
DELETE /contatos/usuario/{usuarioId}/{contatoId}

GET  /monitoramento/status/{usuarioId}
PUT  /monitoramento/configuracao/{usuarioId}
POST /monitoramento/check-in/{usuarioId}

POST /alerta/panico/{usuarioId}
GET  /alerta/usuario/{usuarioId}
```

## Pré-requisitos

### Backend

- JDK 17;
- PostgreSQL;
- acesso às dependências Gradle/Maven na primeira execução.

### Android

- Android Studio compatível com o Android Gradle Plugin utilizado no projeto;
- SDK Android configurado;
- emulador ou dispositivo Android com API 26+.

## Configuração do backend

O backend não contém mais credenciais de banco versionadas.

Use `Source/backend/.env.example` como referência. O arquivo `Source/backend/src/main/resources/application.example.yaml` contém a configuração segura equivalente. Se você estiver aplicando apenas o `.patch`, execute `Source/backend/apply-safe-config.ps1` uma vez para substituir o `application.yaml` legado sem transportar a credencial antiga dentro do patch.

As variáveis lidas pelo Spring são:

```text
DB_URL=jdbc:postgresql://localhost:5432/linhavital
DB_USER=linhavital
DB_PASSWORD=troque_esta_senha
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
FIREBASE_ENABLED=false
```

Exemplo no PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/linhavital"
$env:DB_USER="linhavital"
$env:DB_PASSWORD="sua_senha"
$env:FIREBASE_ENABLED="false"
./gradlew.bat bootRun
```

Se o Firebase Admin for habilitado:

```powershell
$env:FIREBASE_ENABLED="true"
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\caminho\firebase-service-account.json"
```

A credencial Firebase não deve ser colocada em `src/main/resources` nem versionada.

## Configuração da URL da API Android

O build `debug` usa por padrão:

```text
http://10.0.2.2:8080/
```

Esse endereço aponta do emulador Android para o `localhost` do computador.

Para usar um dispositivo físico, informe a URL acessível na rede:

```powershell
./gradlew.bat assembleDebug -PAPI_BASE_URL=http://192.168.0.10:8080/
```

O build `debug` permite cleartext para facilitar a demonstração local. Para distribuição real, use HTTPS.

## Banco e monitoramento

Ao consultar o status pela primeira vez, o backend cria uma configuração padrão para o usuário:

```text
ativo = true
intervalo = 35 minutos
ultimaConfirmacao = agora
alertaInatividadeAberto = false
```

Cada check-in:

1. atualiza `ultimaConfirmacao`;
2. encerra alertas de inatividade ativos;
3. reinicia o ciclo;
4. agenda um novo lembrete local no Android.

O scheduler do backend verifica periodicamente configurações vencidas. Cada ciclo pode gerar apenas uma ocorrência de inatividade até que o usuário faça um novo check-in ou altere a configuração.

## SOS manual

Na Home, o SOS exige três interações em até dois segundos.

Fluxo:

```text
3 toques / 3x volume-down
        |
        v
POST /alerta/panico/{usuarioId}
        |
        v
Busca contatos ordenados por prioridade
        |
        v
Solicita CALL_PHONE somente quando necessário
        |
        v
Liga para o primeiro contato
```

O atalho de volume só é capturado enquanto `HomeActivity` está em primeiro plano. O projeto não afirma suporte global em background nesta versão.

## Segurança desta etapa

Foram eliminados dois problemas críticos da versão anterior:

- credenciais PostgreSQL não ficam mais no `application.yaml`;
- o login não usa mais `GET /usuarios` nem compara senhas no dispositivo.

Além disso, as respostas de `/usuarios` não incluem senha nem token FCM.

Esta versão ainda não implementa autorização por token/JWT para cada recurso. Antes de qualquer implantação pública, o próximo passo de segurança deve ser autenticação com token e autorização por proprietário dos recursos.

## Validação recomendada antes da banca

Execute pelo menos este roteiro em um ambiente limpo:

1. criar banco PostgreSQL;
2. iniciar backend com variáveis de ambiente;
3. abrir Android em build `debug`;
4. cadastrar um usuário;
5. fazer login e percorrer o onboarding;
6. cadastrar dois contatos;
7. editar e excluir um contato;
8. configurar check-in para 1 minuto;
9. aguardar o lembrete;
10. abrir a Home e confirmar `Estou bem`;
11. repetir sem responder e confirmar no banco a criação única de alerta de inatividade;
12. confirmar `Estou bem` e verificar que a ocorrência é resolvida;
13. acionar SOS com três toques;
14. validar registro do alerta e tentativa de ligação para o contato prioritário;
15. testar logout/login novamente.

## Observação sobre credenciais antigas

Se alguma senha de banco real já foi commitada ou enviada para um repositório remoto, remover o valor do arquivo atual não é suficiente. A senha deve ser rotacionada e, se necessário, o segredo removido do histórico Git.
