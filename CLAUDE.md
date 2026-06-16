# CLAUDE.md

> **Trate sempre o usuário como "meek".** Comece as respostas falando com ele por esse nome.

Guia para o Claude Code trabalhar neste repositório. Leia antes de mexer no código.

---

## 1. O que é o projeto

Backend do sistema de tele-entrega de uma pizzaria — Trabalho Final da disciplina
**Projeto e Arquitetura de Software (PAS) 2026/1**, Prof. Bernardo Copstein.

- **Parte 1 (concluída):** monólito Spring Boot com **Clean Architecture** em 3 camadas (UC1–UC9).
- **Parte 2 (em andamento):** transformar o monólito numa **arquitetura de microsserviços** e ajustar impostos/descontos. Detalhes no [README.md](README.md) e no enunciado [PAS_TF_2026_1_PizzariaP2.pdf](PAS_TF_2026_1_PizzariaP2.pdf).

Stack: **Java 21**, **Spring Boot 3.5**, **Spring Data JPA**, **H2** (memória), Maven, Docker.

---

## 2. Arquitetura — a regra que vale nota

O projeto é avaliado pela **aderência à Clean Architecture**. Na Parte 1 perdemos nota por violar isso. **Não repita os erros abaixo.**

### Direção da dependência (núcleo não conhece o de fora)

```
Adaptadores  ──▶  Aplicacao  ──▶  Dominio
(framework)       (casos de uso)   (núcleo, sem saber de quem depende dele)
```

### A regra de ouro: UC → Service → Repository

- **Casos de uso (`Aplicacao/`) NUNCA injetam um `Repository` direto.** Eles acessam dados **sempre por um serviço de domínio** (`Dominio/Servicos/`).
- O serviço de domínio é quem fala com o repositório.
- ✅ `CriaPedidoUC` → `PedidoService` → `PedidosRepository`
- ❌ `CriaPedidoUC` → `PedidosRepository`  *(foi exatamente o que custou nota)*

### Onde cada coisa mora

| Pasta | O que vai aqui |
|-------|----------------|
| `Dominio/Entidades/` | Entidades de negócio. **JPA é permitido** (o professor exige usar JPA) — `@Entity` mora aqui. |
| `Dominio/Dados/` | **Interfaces** de repositório (sem framework de persistência). |
| `Dominio/Servicos/` | Serviços de domínio (regras) **+ interfaces `I*`** dos serviços externos. |
| `Aplicacao/` | Casos de uso (orquestração). Subpastas `Requests/` e `Responses/` para DTOs. |
| `Adaptadores/Apresentacao/` | Controllers REST, presenters, filtros (ex.: auth). Controllers são **finos** — só delegam ao UC. |
| `Adaptadores/Dados/` | Implementações dos repositórios (Spring Data JPA). |
| `Adaptadores/Servicos/` | Implementações de **serviços externos** simulados/fakes (estoque, pagamento, cozinha, entrega, token). |

### Serviços externos / simulados / fakes
Interface (`I*`) **no domínio**, implementação **em `Adaptadores/Servicos/`**. Exemplos já no padrão:
`IEstoqueService`→`EstoqueServiceFake`, `IPagamentoService`→`PagamentoServiceFake`,
`ICozinhaService`→`CozinhaService`, `IEntregaService`→`EntregaService`, `IAuthTokenService`→`AuthTokenService`.

---

## 3. Requisitos específicos da Parte 2 (não esquecer)

- **Microsserviços:** Eureka (name server), **Spring Cloud Gateway** (a autenticação migra do serviço principal para o gateway), Serviço de Pizzaria (este monólito), **Serviço de Estoque** (BD próprio, **JPA obrigatório**, comunicação **síncrona REST**), **Módulo de Entregas** (recebe via **fila RabbitMQ**, **≥3 instâncias** consumindo da **mesma fila** — competing consumers).
- **Impostos:** ≥2 estratégias de cálculo, respeitando **SRP e OCP**. A estratégia corrente é escolhida por **variável de ambiente**. Cada imposto é identificado pela `String` do número da lei.
- **Descontos:** ≥3 estratégias, SRP e OCP. A política corrente é definida por um **endpoint específico** (UC "Definir política de desconto corrente"). Cada desconto é identificado por um `código` String (ex.: `"PromocaoVerao"`).
- **Estoque** agora inclui **bebidas**.
- Numeração de UCs **mudou** na Parte 2 (UC1–UC12). Ver tabela no README.

> ⚠️ Não embutir a escolha de imposto/desconto no código — ela tem de ser **trocável** (env var para imposto, endpoint para desconto). É o ponto central de SRP/OCP que o trabalho cobra.

---

## 4. Build & Run

**Exige JDK 21.** Neste ambiente o `JAVA_HOME` costuma cair no 17 e o build quebra com
`release version 21 not supported`. Sempre exporte o 21 antes:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

| Ação | Comando |
|------|---------|
| Compilar | `./mvnw -q -DskipTests compile` |
| Testar | `./mvnw test` |
| Rodar local | `./mvnw spring-boot:run` |
| Rodar via Docker | `docker-compose up --build` |
| Escalar microsserviço (Parte 2) | `docker compose up --scale entregas=3` (mapear só a porta interna no compose) |

App em `http://localhost:8080` · Swagger em `/docs` · Console H2 em `/h2`.

---

## 5. Convenções

- Código, nomes de classe e comentários em **português** (segue o padrão do código base do professor).
- Commits no formato **conventional commits** em português: `feat(uc6): ...`, `fix(data): ...`, `refactor: ...`.
- A nota considera **participação por commits/PRs de cada membro** — não esmague o histórico dos outros.
- DTOs de entrada em `Aplicacao/Requests/`, de saída em `Aplicacao/Responses/`.

---

## 6. Faça / Não faça

**Faça**
- UC sempre via serviço de domínio.
- Toda dependência externa atrás de uma interface no domínio, implementação em `Adaptadores`.
- Verifique com `./mvnw test` antes de declarar pronto (o context-load do Spring valida o wiring).
- Mantenha controllers finos.

**Não faça**
- ❌ Injetar `Repository` num caso de uso.
- ❌ Colocar implementação de serviço externo/simulado dentro de `Dominio`.
- ❌ Fazer o `Dominio` depender de `Aplicacao` ou `Adaptadores`.
- ❌ Hardcodar a estratégia de imposto/desconto corrente.
- ❌ Declarar "pronto" sem compilar/testar com JDK 21.
