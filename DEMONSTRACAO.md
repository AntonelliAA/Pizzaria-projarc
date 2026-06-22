# Pizzaria — Estrutura, Como Testar e Roteiro de Demonstração

Guia único para entender o sistema, rodar, testar e **apresentar ao professor** o que já foi
feito da Parte 2 (PAS 2026/1).

> Status em 22/06/2026 — arquitetura de microsserviços completa: Eureka, Gateway (auth JWT),
> Pizzaria, **Estoque (JPA, REST)** e **Entregas (RabbitMQ, 3 instâncias)**.

---

## 1. Visão geral da arquitetura

São **5 serviços** + o broker RabbitMQ, orquestrados por Docker Compose e descobertos via Eureka.
O acesso externo passa **somente pelo gateway**.

```
   Cliente / Admin
        │ HTTP + JWT
        ▼
┌─────────────────┐     registra/descobre tudo     ┌──────────────────────┐
│  GATEWAY :8080  │ ─────────────────────────────▶ │ EUREKA (name server) │
│  (auth JWT)     │                                 │        :8761         │
└────────┬────────┘                                 └──────────────────────┘
         │ lb://pizzaria-service
         ▼
┌────────────────────────────┐   REST síncrono    ┌─────────────────────────┐
│  PIZZARIA-SERVICE :8081     │ ─────────────────▶ │  ESTOQUE-SERVICE :8082  │
│  (Clean Architecture)       │  lb://estoque-...  │  (JPA, BD próprio)      │
│                             │                    └─────────────────────────┘
│  ao pagar → cozinha → PRONTO│
└──────────┬──────────────────┘
           │ publica id na fila "pedidos.prontos"
           ▼
      ┌──────────┐   consumo competitivo   ┌────────────────────────────┐
      │ RabbitMQ │ ──────────────────────▶ │ ENTREGAS-SERVICE (x3)       │
      │  :5672   │                          │ atualiza status via REST    │
      └──────────┘                          │ (PUT /internal/pedidos/...) │
                                            └────────────────────────────┘
```

| Serviço | Porta | Papel |
|---------|-------|-------|
| **eureka-server** | 8761 | Name server (descoberta) |
| **gateway** | 8080 | Entrada única + **autenticação JWT** |
| **pizzaria-service** | 8081 (interno) | Regras de negócio (Clean Architecture) |
| **estoque-service** | 8082 (interno) | Estoque isolado, **BD próprio + JPA**, REST síncrono, inclui bebidas |
| **entregas-service** | aleatória, **3 instâncias** | Consome a fila e conclui a entrega (simulado) |
| **rabbitmq** | 5672 / 15672 | Broker de mensagens entre pizzaria e entregas |

### Estrutura de pastas

```
Projarc/
├── docker-compose.yml          # orquestra os 5 serviços + rabbitmq (entregas com replicas: 3)
├── eureka-server/              # name server
├── gateway/                    # Spring Cloud Gateway + auth JWT (camadas: apresentacao/aplicacao/servicos)
├── pizzaria-service/           # monólito em Clean Architecture
│   └── src/main/java/.../ex4_lancheriaddd_v1/
│       ├── Dominio/            # núcleo: Entidades, Dados (interfaces), Servicos (regras + I* externos)
│       ├── Aplicacao/          # casos de uso (UCs) + Requests/Responses
│       └── Adaptadores/        # Apresentacao (controllers), Dados (JPA), Servicos (estoque REST, entrega RabbitMQ, fakes)
├── estoque-service/            # microsserviço de estoque (JPA + H2 próprio)
└── entregas-service/           # microsserviço de entregas (consumidor RabbitMQ)
```

### A regra de arquitetura que vale nota
```
Adaptadores  ──▶  Aplicacao  ──▶  Dominio        (a dependência sempre aponta para dentro)
```
- Caso de uso **nunca** injeta `Repository` direto — sempre via **serviço de domínio** (`UC → Service → Repository`).
- O `Dominio` não importa nada de `Adaptadores`.
- Serviços externos (estoque, pagamento, cozinha, entrega) têm **interface `I*` no domínio** e
  implementação em `Adaptadores/Servicos` (estoque por REST, entrega por RabbitMQ, fakes para os demais).

---

## 2. Como executar

> **Exige JDK 21.** Se o build reclamar de "release version 21": `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`

### Opção A — Docker Compose (recomendado; sobe tudo, inclusive RabbitMQ)

```bash
docker compose up --build
# 3 instâncias de entregas:
docker compose up --build --scale entregas-service=3
```

Acesso público em `http://localhost:8080`. Dashboards: Eureka `:8761`, RabbitMQ `:15672` (guest/guest).

### Opção B — Maven local (sem entregas/RabbitMQ)

Para validar tudo **menos** a fila (estoque via REST funciona):
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd eureka-server    && ./mvnw spring-boot:run    # :8761
cd estoque-service  && ./mvnw spring-boot:run    # :8082
cd pizzaria-service && ./mvnw spring-boot:run    # :8081
cd gateway          && ./mvnw spring-boot:run    # :8080
```
(O fluxo de entregas precisa do RabbitMQ + entregas-service — use a Opção A.)

---

## 3. Passo a passo de teste (via gateway)

Importe a coleção [insomnia.json](insomnia.json) no Insomnia, **ou** use os `curl` abaixo.
Clientes pré-carregados: `huguinho.pato@email.com` / `zezinho.pato@email.com` — senha `senha`.

```bash
B=http://localhost:8080

# 1) Login (UC12) — gateway emite o JWT
TOKEN=$(curl -s -X POST $B/auth -H "Content-Type: application/json" \
  -d '{"email":"huguinho.pato@email.com","senha":"senha"}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

# 2) Cardápio protegido: sem token -> 401, com token -> 200
curl -s -o /dev/null -w "sem=%{http_code}\n" $B/cardapio/1
curl -s -o /dev/null -w "com=%{http_code}\n" -H "Authorization: Bearer $TOKEN" $B/cardapio/1

# 3) Admin (UC1–UC4)
curl -s -H "Authorization: Bearer $TOKEN" $B/admin/cardapios                       # UC1
curl -s -X PUT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"cardapioId":2}' $B/admin/cardapios/definir-corrente                        # UC2
curl -s -H "Authorization: Bearer $TOKEN" $B/admin/descontos/politicas            # UC3
curl -s -X PUT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"codigo":"promocao_verao"}' $B/admin/descontos/definir-corrente            # UC4

# 4) Pedido (UC6): verifica estoque via REST -> estoque-service e calcula preço
curl -s -X POST $B/pedidos -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"clienteCpf":"9001","enderecoEntrega":"Rua A","itens":[{"produtoId":1,"quantidade":2},{"produtoId":3,"quantidade":1}]}'

# 5) Pagar (UC9): envia pra cozinha; ao ficar PRONTO publica na fila; entregas conclui (RabbitMQ)
curl -s -X PUT -H "Authorization: Bearer $TOKEN" $B/pedidos/<id>/pagar
# acompanhe a evolução do status:
curl -s -H "Authorization: Bearer $TOKEN" $B/pedidos/<id>/status
```

### Trocar a estratégia de imposto (por variável de ambiente)
O imposto corrente é escolhido por `imposto.politica` (env `IMPOSTO_POLITICA`). Valores: `Lei10Porcento`
(padrão) ou `Lei5Porcento`. Ex.: no `docker-compose.yml` do `pizzaria-service`,
`environment: IMPOSTO_POLITICA: Lei5Porcento`.

### Suíte automatizada e testes unitários
```bash
python3 test_api.py http://localhost:8080         # end-to-end pelo gateway (44/44)
cd pizzaria-service && ./mvnw test                # AutenticarUC + context-load (wiring)
cd gateway          && ./mvnw test                # JwtTokenService, LoginUC, RotasPublicas
```

---

## 4. O que mostrar ao professor (roteiro)

1. **Microsserviços + descoberta** — `http://localhost:8761`: mostre os 5 serviços registrados
   (gateway, pizzaria, estoque, entregas — 3 instâncias). Acesso externo só pelo gateway.
2. **Autenticação no gateway** — login retorna JWT; rota protegida dá **401 sem token / 200 com token**.
   Reforce: a auth **saiu** do serviço principal (lá só há o `/internal/auth/validar`).
3. **Clean Architecture** — abra a árvore: `Aplicacao` (UC) → `Dominio/Servicos` → `Dominio/Dados`.
   Nenhum UC injeta repositório; `Dominio` não importa `Adaptadores`; serviços externos atrás de `I*`.
4. **Estoque isolado (REST + JPA)** — submeta um pedido e mostre no log/console H2 (`:8082/h2`) a
   **baixa no estoque** num **banco separado**. Comunicação **síncrona** pizzaria→estoque. Inclui **bebidas**.
5. **Entregas por fila (RabbitMQ, 3 instâncias)** — pague um pedido e mostre, nos logs das 3 instâncias
   do `entregas-service`, que **apenas uma** pega cada pedido da fila `pedidos.prontos` (consumo
   competitivo) e o status evolui até **ENTREGUE**. Dashboard do RabbitMQ em `:15672`.
6. **Impostos e descontos (SRP/OCP)** — `GET /admin/descontos/politicas` lista 3 estratégias; troque a
   corrente por **endpoint** (UC4). Mostre que o **imposto** troca por **variável de ambiente** (2 estratégias).
7. **Tudo verde** — `python3 test_api.py http://localhost:8080` → **44/44**.

---

## 5. O que já está pronto × o que falta

### ✅ Pronto
- **Infra:** Eureka, Gateway com **auth JWT migrada**, monólito conteinerizado e registrado.
- **Clean Architecture** revisada (UC → Service → Repository; serviços externos atrás de interface).
- **UC1–UC12** (admin de cardápios e descontos, carregar cardápio, pedido, status, cancelar, pagar,
  entregues, cadastro, login).
- **Descontos:** 3 estratégias (SRP/OCP); política corrente trocável por **endpoint** (UC4).
- **Impostos:** 2 estratégias; corrente escolhida por **variável de ambiente**; identificadas pela lei.
- **Estoque:** microsserviço próprio com **JPA + BD isolado**, REST **síncrono**, com **bebidas** e baixa real.
- **Entregas:** microsserviço próprio consumindo de **RabbitMQ** com **3 instâncias** (consumo competitivo).

### ⏳ Pontos de atenção / a fazer
- **Demonstrar load balancer** com múltiplas instâncias dos demais serviços (marco 29/06).
- **Limitação conhecida (caminho de falta de estoque):** o `estoque-service` devolve a *descrição do
  ingrediente* em falta, mas a pizzaria casa essa lista com a *descrição do produto* ao montar a
  resposta 422 — a marcação de "produto indisponível" pode não bater. O caminho feliz (com estoque)
  funciona; ajustar o mapeamento ingrediente→produto fica como melhoria.

---

## 6. Documentos relacionados
- [README.md](README.md) · [CLAUDE.md](CLAUDE.md) · [enunciado da Parte 2](PAS_TF_2026_1_PizzariaP2.pdf)
- Design e plano do incremento de infra em [docs/superpowers/](docs/superpowers/).
