# Painel Web de Tarefas

Aplicação web server-rendered simples (estilo to-do list) em Java com Spring Boot MVC + Thymeleaf e PostgreSQL, testada ponta a ponta com Selenium.

## Status

✅ MVP implementado.

## Stack

- Java 17 + Spring Boot 3.3 (MVC + Thymeleaf)
- PostgreSQL + Flyway
- Lombok (na entidade `Task`)
- Gradle (Kotlin DSL) + wrapper `gradlew`
- Testcontainers (Postgres nos testes de integração, browser Chrome em container nos testes E2E) + Selenium + JUnit 5 + Mockito

## Como rodar

1. Suba o PostgreSQL:
   ```bash
   docker compose up -d
   ```
2. Rode a aplicação:
   ```bash
   ./gradlew bootRun
   ```
3. Acesse `http://localhost:8080` (redireciona para `/tasks`).

## Como rodar os testes

```bash
./gradlew test
```

- Testes unitários (`service`) não precisam de Docker.
- Testes de integração (`integration`) sobem um PostgreSQL real via Testcontainers.
- Testes E2E (`e2e`) sobem PostgreSQL **e** um navegador Chrome real, ambos em containers via Testcontainers (`testcontainers-selenium`), e dirigem o navegador contra a aplicação real rodando na porta aleatória de teste — não é necessário ter Chrome instalado na máquina.

Suíte completa: **10 testes, todos passando** — 6 unitários, 3 de integração e 1 E2E dirigindo um Chrome real em container.

### Nota sobre Testcontainers e Docker Engine recente

Se os testes falharem com `client version 1.32 is too old. Minimum supported API version is 1.40`, a causa é o `docker-java` embutido no Testcontainers negociar a API 1.32, abaixo do mínimo aceito pelo Docker Engine 29+. Correção global, de uma linha:

```bash
echo 'api.version=1.44' > ~/.docker-java.properties
```

### Nota sobre a ordem de criação do container do browser

O `BrowserWebDriverContainer` **não** é declarado como `@Container` estático, e isso é deliberado. O `Testcontainers.exposeHostPorts(port)` torna o host alcançável de dentro dos containers pelo nome `host.testcontainers.internal`, mas esse mapeamento é aplicado a cada container **no momento em que ele é criado**. Um browser declarado como `@Container` estático sobe antes de qualquer método de teste rodar — portanto antes de a porta aleatória da aplicação existir — e nunca receberia o mapeamento: todo `driver.get` falharia com `ERR_NAME_NOT_RESOLVED`. Criá-lo sob demanda, depois do `exposeHostPorts`, é o que faz o nome resolver. Usar uma `server.port` fixa permitiria o container estático, ao custo de colidir com o que o desenvolvedor já tem rodando.

## Fluxo da aplicação

```
Browser → Spring MVC Controller → Service → JPA Repository → PostgreSQL
                  ↓
         View Thymeleaf (HTML renderizado, sem JS de frontend)
```

Cada ação (criar, concluir, excluir) é um `POST` de formulário HTML tradicional, seguido de um redirect para `GET /tasks` — não há chamadas AJAX/JSON, propositalmente, para manter o exemplo o mais próximo possível de uma aplicação web server-rendered clássica.

## Páginas e rotas

| Método | Rota                    | Descrição                          |
|--------|--------------------------|--------------------------------------|
| GET    | `/tasks`                | Lista de tarefas (view Thymeleaf)    |
| POST   | `/tasks`                | Cria uma nova tarefa (`title`)       |
| POST   | `/tasks/{id}/complete`  | Marca a tarefa como concluída        |
| POST   | `/tasks/{id}/delete`    | Remove a tarefa                      |

## Seletores usados nos testes E2E

| Seletor                 | Elemento                                   |
|--------------------------|---------------------------------------------|
| `#task-title-input`     | Campo de texto para o título da nova tarefa |
| `#add-task-button`      | Botão de adicionar tarefa                   |
| `.task-item`             | Cada item da lista de tarefas               |
| `.task-item.completed`  | Item marcado como concluído (com risco)     |
| `.task-title`            | Título da tarefa dentro do item             |
| `.complete-button`      | Botão "Complete" de um item                 |
| `.delete-button`        | Botão "Delete" de um item                   |
| `#empty-state`           | Mensagem exibida quando não há tarefas      |
