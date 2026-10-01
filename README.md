# Painel Web de Tarefas

Aplicação web de lista de tarefas renderizada no servidor, com Spring MVC e Thymeleaf, testada ponta a ponta com Selenium dirigindo um navegador real.

Cada ação — criar, concluir, excluir — é um POST de formulário HTML seguido de redirect, sem JavaScript de frontend. O foco do projeto é a pirâmide de testes de uma aplicação server-rendered: unidade no serviço, integração no controller com banco real e E2E pelo navegador.

## Tecnologias e bibliotecas

| | |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.3 (Spring MVC) |
| Views | Thymeleaf |
| Persistência | Spring Data JPA, PostgreSQL 16 |
| Migrations | Flyway |
| Build | Gradle Kotlin DSL (wrapper `gradlew`) |
| Testes | JUnit 5, Mockito, Selenium 4, Testcontainers (PostgreSQL e Chrome) |
| Apoio | Lombok |

## Pré-requisitos

- JDK 17 ou superior
- Docker (para o banco e para o navegador dos testes E2E)

## Como rodar

```bash
docker compose up -d
```

```bash
./gradlew bootRun
```

Acesse `http://localhost:8080` — a raiz redireciona para `/tasks`.

## Rotas

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/tasks` | Lista de tarefas |
| `POST` | `/tasks` | Cria uma tarefa (campo `title`) |
| `POST` | `/tasks/{id}/complete` | Marca como concluída |
| `POST` | `/tasks/{id}/delete` | Remove a tarefa |

## Testes

```bash
./gradlew test
```

10 testes em três camadas:

- **Unitários** — regras do serviço, sem dependência externa.
- **Integração** — controller e repositório contra um PostgreSQL em container.
- **E2E** — um Chrome real, também em container, navegando na aplicação. Não é preciso ter o navegador instalado na máquina.

Os testes E2E se apoiam em seletores estáveis no HTML:

| Seletor | Elemento |
|---|---|
| `#task-title-input` | Campo do título da nova tarefa |
| `#add-task-button` | Botão de adicionar |
| `.task-item` | Item da lista |
| `.task-item.completed` | Item concluído |
| `.complete-button` / `.delete-button` | Ações do item |
| `#empty-state` | Mensagem de lista vazia |
