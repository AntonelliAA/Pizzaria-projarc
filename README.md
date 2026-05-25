# Pizzaria — Sistema de Tele-Entrega

Trabalho final da disciplina **Projeto e Arquitetura de Software (PAS) — 2026/1**.
Sistema de pedidos online para uma pizzaria, implementado em Spring Boot 3 + Java 21 com arquitetura limpa (Clean Architecture) em 3 camadas.

> 📄 O enunciado original do trabalho está em [enunciado.md](enunciado.md).

---

## 📦 Stack

- **Java 21** + **Spring Boot 3.5**
- **Spring Web** (REST) · **Spring Data JPA** · **H2** (banco em memória)
- **Swagger / OpenAPI** (documentação da API)
- **BCrypt** (hash de senhas)
- **Maven** (build) · **Docker** (execução)

---

## 🚀 Como executar

### Opção A — Docker (recomendado)

```bash
docker-compose up --build
```

A aplicação sobe em `http://localhost:8080`.

### Opção B — Maven local

```bash
./mvnw spring-boot:run
```

### Acessar

| Recurso | URL |
|---------|-----|
| API base | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Console H2 | http://localhost:8080/h2 (jdbc: `jdbc:h2:mem:testdb`, user `sa`, sem senha) |

---

## 🗂️ Arquitetura

Estrutura em pacotes seguindo Clean Architecture com 3 camadas isoladas:

```
src/main/java/com/bcopstein/ex4_lancheriaddd_v1/
├── Dominio/                # Regras de negócio puras (sem framework)
│   ├── Entidades/          # Cliente, Pedido, Produto, Cardapio, ...
│   ├── Dados/              # Interfaces de repositório
│   └── Servicos/           # Interfaces (I*) + implementações de domínio
├── Aplicacao/              # Casos de uso (orquestração) + DTOs
│   ├── Requests/           # DTOs de entrada
│   └── Responses/          # DTOs de saída
└── Adaptadores/            # Interface com o mundo externo
    ├── Apresentacao/       # Controllers REST + presenters
    ├── Dados/              # Implementações JDBC dos repositórios
    └── Servicos/           # Fakes de serviços externos (estoque, pagamento)
```

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

Cozinha e Entrega são serviços **simulados** ([CozinhaService](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Dominio/Servicos/CozinhaService.java) / [EntregaService](src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Dominio/Servicos/EntregaService.java)) que avançam o status no banco a cada 2 segundos via `ScheduledExecutorService`.

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

## 🧪 Validação

O script [test_api.py](test_api.py) cobre **44 cenários** (UC1–UC9 + casos de erro + auth) e roda 100% em ambiente local com `python3 test_api.py`.

## ⚠️ Notas

- O serviço de estoque é um *fake* que sempre retorna disponível — o ramo de "marcar produto indisponível" em UC4 só dispararia se o fake fosse substituído.
- Preços são armazenados como `bigint` em reais inteiros (ex: `5500` = R$ 5500,00).
- Tokens de sessão expiram em **8 horas** e são mantidos em memória (`ConcurrentHashMap`) — reiniciar a aplicação invalida todos os tokens emitidos.
