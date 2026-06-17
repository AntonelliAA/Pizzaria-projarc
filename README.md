# Pizzaria — Sistema de Tele-Entrega

Trabalho final da disciplina **Projeto e Arquitetura de Software (PAS) — 2026/1**.
Sistema de pedidos online para uma pizzaria, implementado em Spring Boot 3 + Java 21 com arquitetura limpa (Clean Architecture) em 3 camadas.

O trabalho tem duas partes:
- **Parte 1 (concluída):** o monólito com Clean Architecture descrito abaixo.
- **Parte 2 (em andamento):** evoluir o monólito para uma **arquitetura de microsserviços** e ajustar os serviços de impostos e descontos — ver seção [🧭 Parte 2](#-parte-2--arquitetura-de-microsserviços).

> 📄 Enunciados: Parte 1 em [enunciado.md](enunciado.md) · Parte 2 em [PAS_TF_2026_1_PizzariaP2.pdf](PAS_TF_2026_1_PizzariaP2.pdf).

---

## 📦 Stack

- **Java 21** + **Spring Boot 3.5**
- **Spring Web** (REST) · **Spring Data JPA** · **H2** (banco em memória)
- **Swagger / OpenAPI** (documentação da API)
- **BCrypt** (hash de senhas)
- **Maven** (build) · **Docker** (execução)

---

## 🚀 Como executar

> A partir da Parte 2 o sistema é composto por **3 serviços** (Eureka + Gateway + Pizzaria).
> O acesso externo é **somente pelo gateway** em `http://localhost:8080`.

### Opção A — Docker Compose (recomendado)

```bash
docker compose up --build
```

Sobe Eureka (`:8761`), o serviço de pizzaria (interno, `:8081`) e o gateway (`:8080`).

### Opção B — Maven local (3 terminais, exige JDK 21)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd eureka-server    && ./mvnw spring-boot:run   # :8761
cd pizzaria-service && ./mvnw spring-boot:run   # :8081
cd gateway          && ./mvnw spring-boot:run   # :8080
```

### Acessar

| Recurso | URL |
|---------|-----|
| API (via gateway) | http://localhost:8080 |
| Dashboard Eureka | http://localhost:8761 |
| Swagger UI (pizzaria) | http://localhost:8080/docs |
| Console H2 (pizzaria) | http://localhost:8081/h2 (jdbc: `jdbc:h2:mem:pizzadb`, user `sa`, sem senha) |

### Autenticação (no gateway)

```bash
# login (UC12) — devolve um JWT
TOKEN=$(curl -s -X POST http://localhost:8080/auth \
  -H "Content-Type: application/json" \
  -d '{"email":"huguinho.pato@email.com","senha":"senha"}' | jq -r .token)

# rota protegida com o JWT
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/cardapio/1
```

Rotas abertas: `POST /clientes` (cadastro), `POST /auth` (login), Swagger. As demais exigem `Authorization: Bearer <jwt>` — sem token, o gateway responde **401**.

---

## 🗂️ Arquitetura

Estrutura em pacotes seguindo Clean Architecture com 3 camadas isoladas:

```
src/main/java/com/bcopstein/ex4_lancheriaddd_v1/
├── Dominio/                # Regras de negócio (núcleo)
│   ├── Entidades/          # Cliente, Pedido, Produto, Cardapio, ...
│   ├── Dados/              # Interfaces de repositório
│   └── Servicos/           # Serviços de domínio + interfaces (I*) dos serviços externos
├── Aplicacao/              # Casos de uso (orquestração) + DTOs
│   ├── Requests/           # DTOs de entrada
│   └── Responses/          # DTOs de saída
└── Adaptadores/            # Interface com o mundo externo
    ├── Apresentacao/       # Controllers REST + presenters + filtro de auth
    ├── Dados/              # Implementações dos repositórios (Spring Data JPA)
    └── Servicos/           # Serviços externos simulados/fakes (estoque, pagamento, cozinha, entrega, token de auth)
```

> **Regra de dependência:** os casos de uso (Aplicacao) acessam os dados sempre por um **serviço de domínio** (`Dominio/Servicos`), nunca pelo repositório direto. Os serviços externos simulados/fakes têm interface no domínio (`I*`) e implementação trocável em `Adaptadores/Servicos`.

Diagramas PlantUML na raiz: [Dominio.puml](Dominio.puml), [Atores.puml](Atores.puml), [ContextoC1.puml](ContextoC1.puml), [DrgClasses4camadas.puml](DrgClasses4camadas.puml), [OrganizacaoEmPacotes.puml](OrganizacaoEmPacotes.puml).

---

## ✅ Casos de Uso Implementados

| UC | Descrição | Endpoint | Classe |
|----|-----------|----------|--------|
| UC1 | Registrar cliente | `POST /clientes` | [RegistrarClienteUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/RegistrarClienteUC.java) |
| UC2 | Autenticar | `POST /auth` | [AutenticarUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/AutenticarUC.java) |
| UC3 | Carregar cardápio corrente | `GET /cardapio/{id}` | [RecuperarCardapioUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/RecuperarCardapioUC.java) |
| UC4 | Submeter pedido para aprovação | `POST /pedidos` | [CriaPedidoUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/CriaPedidoUC.java) |
| UC5 | Consultar status do pedido | `GET /pedidos/{id}/status` | [ConsultaStatusPedidoUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/ConsultaStatusPedidoUC.java) |
| UC6 | Cancelar pedido aprovado | `PUT /pedidos/{id}/cancelar` | [CancelaPedidoUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/CancelaPedidoUC.java) |
| UC7 | Pagar pedido | `PUT /pedidos/{id}/pagar` | [PagarPedidoUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/PagarPedidoUC.java) |
| UC8 | Listar pedidos entregues entre datas | `GET /pedidos/entregues?inicio=...&fim=...` | [ListaPedidosEntreguesUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/ListaPedidosEntreguesUC.java) |
| UC9 | Listar pedidos entregues de um cliente | `GET /pedidos/entregues/{cpf}?inicio=...&fim=...` | [ListaPedidosEntreguesClienteUC](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/ListaPedidosEntreguesClienteUC.java) |

---

## 💰 Regras de Negócio

| Regra | Valor | Onde |
|-------|-------|------|
| Imposto | **10%** sobre soma dos itens | [ImpostosServiceImpl](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Dominio/Servicos/ImpostosServiceImpl.java) |
| Desconto de fidelidade | **7%** se cliente tem **> 3 pedidos** nos **últimos 20 dias** | [DescontosServiceImpl](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Dominio/Servicos/DescontosServiceImpl.java) |
| Custo final | `(soma dos itens − desconto) + imposto` | [CriaPedidoUC:84](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/CriaPedidoUC.java#L84) |

> As fórmulas de imposto/desconto estão **isoladas em serviços com interface** (`IImpostosService`, `IDescontosService`) para facilitar mudanças futuras sem afetar o resto do sistema.

---

## 🔄 Fluxo de Status do Pedido

```
NOVO ──(estoque OK)──▶ APROVADO ──(pagamento)──▶ PAGO
                          │                         │
                       (cancelar)                   ▼
                          │                    AGUARDANDO
                          ▼                         │
                      (descartado)                  ▼
                                              PREPARACAO
                                                    │
                                                    ▼
                                                 PRONTO
                                                    │
                                                    ▼
                                              TRANSPORTE
                                                    │
                                                    ▼
                                                ENTREGUE
```

Cozinha e Entrega são serviços **simulados** ([CozinhaService](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Adaptadores/Servicos/CozinhaService.java) / [EntregaService](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Adaptadores/Servicos/EntregaService.java)) que avançam o status no banco a cada 2 segundos via `ScheduledExecutorService`.

---

## 🧩 Serviços com interface (troca futura)

Todos os serviços abaixo têm interface no domínio (`I*`) e implementação trocável:

| Interface | Implementação atual | Tipo |
|-----------|---------------------|------|
| `IEstoqueService` | `EstoqueServiceFake` | Fake (sempre OK) |
| `IPagamentoService` | `PagamentoServiceFake` | Fake (sempre aprova) |
| `ICozinhaService` | `CozinhaService` | Simulado (assíncrono) |
| `IEntregaService` | `EntregaService` | Simulado (assíncrono) |
| `IImpostosService` | `ImpostosServiceImpl` | Real (10%) |
| `IDescontosService` | `DescontosServiceImpl` | Real (7% fidelidade) |

---

## 🗄️ Banco de Dados

Schema H2 carregado de [schema.sql](src/main/resources/schema.sql); dados de teste em [data.sql](src/main/resources/data.sql).

### Dados pré-carregados

- **2 clientes**: `huguinho.pato@email.com` e `zezinho.pato@email.com` (senha: `senha`)
- **3 produtos**: Pizza calabresa, queijo e presunto, margherita
- **2 cardápios**: Agosto (id=1, corrente) e Setembro (id=2)
- **3 pedidos com status ENTREGUE** (datas 20–22/05/2026) para testar UC8 e UC9
- Estoque de 30 unidades para cada um dos 9 ingredientes

---

## 🧪 Teste de API

Existe um script Python que percorre o fluxo end-to-end:

```bash
python3 test_api.py
```

---

## 🔐 Autenticação

Endpoints marcados como 🔒 no enunciado (UC3–UC7) são protegidos por [AuthFilter](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Adaptadores/Apresentacao/AuthFilter.java), que valida o header `Authorization: Bearer <token>` contra o `AuthTokenService`.

Fluxo de uso:

```bash
# 1. Login (UC2) — devolve token
TOKEN=$(curl -s -X POST http://localhost:8080/auth \
  -H "Content-Type: application/json" \
  -d '{"email":"huguinho.pato@email.com","senha":"senha"}' | jq -r .token)

# 2. Chamar endpoint protegido com o token
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/cardapio/1
```

Sem token (ou com token inválido/expirado) a resposta é **HTTP 401** com `{"erro":"..."}`.
Endpoints abertos: `POST /clientes` (UC1), `POST /auth` (UC2), `GET /pedidos/entregues*` (UC8/UC9), Swagger e H2 Console.

---

## 🧭 Parte 2 — Arquitetura de Microsserviços

A Parte 2 tem dois objetivos: **(1)** comprovar as vantagens da arquitetura limpa na manutenção e **(2)** transformar o monólito numa **arquitetura de microsserviços** conteinerizada. O acesso passa por um **gateway** que se registra num **name server**.

### 🏗️ Arquitetura-alvo

```
            Cliente / Cozinha / Administrador / Entregador
                              │ HTTP(S) (auth no gateway)
                    ┌─────────▼─────────┐
                    │  Spring Cloud     │   ← autenticação fica AQUI
                    │     Gateway       │
                    └─────────┬─────────┘
        roteamento ┌──────────┼───────────────┐
                   ▼          ▼                ▼
        ┌──────────────┐  ┌──────────┐   ┌─────────────────┐
        │ Serviço de   │  │ Serviço  │   │ Módulo de       │
        │ Pizzaria     │─▶│ de       │   │ Entregas        │
        │ (monólito P1)│  │ Estoque  │   │ (≥3 instâncias) │
        └──────┬───────┘  │ (JPA,    │   └────────▲────────┘
               │ REST     │ BD próprio)│           │ consome fila
               │ síncrono └────┬─────┘             │
               │ publica       ▼ BD Estoque   ┌────┴─────┐
               │ entrega ─────────────────────▶│ RabbitMQ │
               │                               └──────────┘
               └──────────────┬──────────────────────────┐
                              ▼                            ▼
                        BD Pizzaria              Eureka (Name Server)
                                              (registro/descoberta de todos)
```

| Microsserviço | Papel | Pontos obrigatórios |
|---------------|-------|---------------------|
| **Name Server** | Descoberta de serviços | Eureka (Spring Cloud) |
| **Gateway** | Ponto único de entrada | Spring Cloud Gateway **+ autenticação** (migrada do serviço de pizzaria) |
| **Serviço de Pizzaria** | Monólito da Parte 1 ajustado | Auth **removida** daqui |
| **Serviço de Estoque** | Estoque isolado, BD próprio | **JPA obrigatório**, comunicação **síncrona (REST)** com a pizzaria, inclui **bebidas** |
| **Módulo de Entregas** | Entregas (segue simulado) | Recebe via **fila RabbitMQ**, **≥3 instâncias** competindo na **mesma fila** |
| **RabbitMQ** | Broker de mensagens | Entre pizzaria e entregas |

Cada microsserviço é **conteinerizado** e registrado no Eureka. Escalar instâncias:
`docker compose up --scale entregas=3` (no `compose.yaml`, mapear **só a porta interna**, ex.: `ports: ["8000"]`).

### ✅ Casos de Uso (renumerados na Parte 2)

| UC | Ator | Descrição |
|----|------|-----------|
| UC1 | Adm | Listar cardápios disponíveis |
| UC2 | Adm | **Definir o cardápio corrente** *(novo)* |
| UC3 | Adm | **Listar as políticas de desconto disponíveis** *(novo)* |
| UC4 | Adm | **Definir a política de desconto corrente** *(novo — via endpoint)* |
| UC5 | Cliente | Carregar cardápio |
| UC6 | Cliente | Submeter pedido para aprovação |
| UC7 | Cliente | Solicitar status de pedido |
| UC8 | Cliente | Cancelar pedido (aprovado, não pago) |
| UC9 | Cliente | Pagar pedido (registra estado + data/hora das mudanças) |
| UC10 | Cliente | Listar pedidos entregues entre duas datas |
| UC11 | Anônimo | Cadastrar usuário |
| UC12 | Usuário | Entrar no sistema (autenticação) |

### 🧮 Impostos e Descontos (SRP + OCP)

- **Impostos:** ao menos **2 formas de cálculo**. A forma corrente é escolhida por **variável de ambiente**. Cada imposto é identificado pela `String` do número da lei.
- **Descontos:** ao menos **3 formas de cálculo**. A política corrente é definida por um **endpoint específico** (UC4). Cada desconto é identificado por um `código` (ex.: `"PromocaoVerao"`, `"PromocaoDiaDosPais"`).
- Ambos projetados para **trocar a fórmula com frequência sem impactar o resto** — sem hardcode da estratégia corrente.

### 📅 Cronograma (Parte 2)

| Data | Entrega |
|------|---------|
| 10/06/2026 | Definição do trabalho — estudo de caso rodando |
| 15/06/2026 | Monólito conteinerizado atrás de gateway + name server, com auth no gateway |
| 17/06/2026 | Ajustes nos casos de uso e nos serviços de impostos e descontos |
| 22/06/2026 | Microsserviço de estoque isolado + entregas via fila; **JPA real no estoque** (não mais simulado) |
| 29/06/2026 | Múltiplas instâncias dos microsserviços; load balancer funcionando |
| 01/07/2026 | **Apresentação do trabalho** |

> 📦 Entrega final: `.zip` com todos os fontes no Moodle. Apresentação na data é **obrigatória**.

---

## 🧪 Validação

O script [test_api.py](test_api.py) cobre **44 cenários** (UC1–UC9 + casos de erro + auth) e roda 100% em ambiente local com `python3 test_api.py`.

## ⚠️ Notas

- O serviço de estoque é um *fake* que sempre retorna disponível — o ramo de "marcar produto indisponível" em UC4 só dispararia se o fake fosse substituído.
- Preços são armazenados como `bigint` em reais inteiros (ex: `5500` = R$ 5500,00).
- Tokens de sessão expiram em **8 horas** e são mantidos em memória (`ConcurrentHashMap`) — reiniciar a aplicação invalida todos os tokens emitidos.
