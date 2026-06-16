# Design — Incremento 1: Eureka + Gateway + Autenticação

**Data:** 2026-06-15
**Trabalho:** PAS 2026/1 — Pizzaria, Parte 2 (microsserviços)
**Marco:** 15/06 — "Monólito rodando conteinerizado atrás de gateway + name server, com autenticação através do gateway."

---

## 1. Objetivo

Transformar o monólito da Parte 1 no primeiro recorte da arquitetura de microsserviços:
um **name server** (Eureka), um **gateway** (Spring Cloud Gateway) que concentra a
**autenticação**, e o **serviço de pizzaria** (o monólito) registrado e acessível apenas
através do gateway. Estoque, entregas/RabbitMQ, estratégias de impostos/descontos e os
novos casos de uso de administrador ficam para os marcos seguintes (17/06 e 22/06).

## 2. Princípios de arquitetura a preservar (requisito de primeira classe)

> O trabalho é avaliado pela aderência à arquitetura. Estas regras **não podem se perder**
> ao introduzir os microsserviços.

1. **Clean Architecture intacta no `pizzaria-service`.** A direção da dependência continua
   `Adaptadores → Aplicacao → Dominio`. Remover a autenticação **não pode** vazar para o domínio.
2. **Regra de ouro:** casos de uso acessam dados via **serviço de domínio**, nunca repositório direto.
3. **Auth é detalhe de borda.** Tudo que é HTTP/JWT/header vive na camada de Adaptadores
   (no pizzaria) ou em camada equivalente no gateway — nunca no domínio.
4. **Gateway também em camadas.** Mesmo sendo um app pequeno, o gateway separa:
   apresentação (filtro/controller) · aplicação (orquestração do login) · serviço de token
   atrás de **interface** (`ITokenService`) · adaptador HTTP para chamar a pizzaria.
   Nada de regra de negócio espalhada em filtro.
5. **Serviços externos atrás de interface.** O gateway fala com a pizzaria por um
   adaptador cuja interface é definida na camada de aplicação do gateway (troca futura sem dor).

## 3. Estrutura do repositório

O monólito atual é movido para `pizzaria-service/` com `git mv` (preserva histórico).
Dois apps Spring Boot irmãos são criados na raiz:

```
Projarc/
├── docker-compose.yml      # orquestra: eureka → gateway + pizzaria
├── eureka-server/          # Name Server (Eureka)              :8761
├── gateway/                # Spring Cloud Gateway + auth (JWT)  :8080  (única porta pública)
├── pizzaria-service/       # monólito da Parte 1, sem auth      :8081  (interno)
├── README.md · CLAUDE.md · enunciado.md · PAS_TF_2026_1_PizzariaP2.pdf · *.puml
```

Cada serviço é um projeto Maven independente, com seu próprio `pom.xml`, `Dockerfile`
e wrapper Maven.

## 4. Versões e dependências

- **Spring Boot 3.5.4** (já em uso no monólito).
- **Spring Cloud 2024.0.x** (trem compatível com Boot 3.5) via BOM `spring-cloud-dependencies`.
- Eureka: `spring-cloud-starter-netflix-eureka-server` (name server) e
  `spring-cloud-starter-netflix-eureka-client` (gateway e pizzaria).
- Gateway: `spring-cloud-starter-gateway` (reativo / WebFlux).
- JWT: `io.jsonwebtoken:jjwt` (assinatura HS256).

> Validar a compatibilidade exata Boot↔Cloud no início (é o risco técnico nº 1).

## 5. Componentes

### 5.1 Eureka (Name Server) — `eureka-server/`
App mínimo com `@EnableEurekaServer`, porta **8761**, configurado para **não se registrar
em si mesmo** (`register-with-eureka: false`, `fetch-registry: false`). Dashboard em
`http://localhost:8761`.

### 5.2 Gateway — `gateway/`
Entrada única pública (**:8080**), registrado no Eureka. Responsabilidades:

**Roteamento** (descoberta via Eureka, `lb://pizzaria-service`):
`/clientes/**`, `/cardapio/**`, `/pedidos/**`, `/docs/**` → pizzaria.

**Login (UC12)** — `POST /auth` é tratado **no gateway**:
1. Recebe `{email, senha}`.
2. Chama (HTTP síncrono, WebClient) o endpoint interno da pizzaria `POST /internal/auth/validar`.
3. A pizzaria confere as credenciais (BCrypt) e devolve `{cpf, email}` se válidas (ou 401).
4. O gateway **emite um JWT assinado** (HS256, claim `cpf`, expiração configurável) e
   devolve `{token, cpf, email}`.

**Filtro global de autenticação** — nas rotas protegidas:
- Exige `Authorization: Bearer <jwt>`; valida assinatura e expiração.
- Em caso de sucesso, injeta `X-Cliente-Cpf: <cpf>` no request encaminhado ao downstream.
- Sem token / token inválido → **401** com corpo `{"erro": "..."}`.

**Rotas abertas** (sem JWT): `POST /auth`, `POST /clientes` (UC11), Swagger.

**Organização interna do gateway (camadas):**
- `apresentacao/` — `AuthController` (`POST /auth`) e o `AuthGlobalFilter`.
- `aplicacao/` — `LoginUC` (orquestra validação + emissão de token).
- `servicos/` — `ITokenService` (interface) + `JwtTokenService` (impl HS256);
  `ICredenciaisClient` (interface) + `PizzariaCredenciaisClient` (adapter WebClient).
- Chave de assinatura e expiração via **variável de ambiente**.

### 5.3 Pizzaria Service — `pizzaria-service/`
O monólito da Parte 1, com a autenticação **retirada**:

**Remoções (auth sai do serviço principal):**
- `Adaptadores/Apresentacao/AuthFilter` — validação de token agora é do gateway.
- `Adaptadores/Servicos/AuthTokenService` e `Dominio/Servicos/IAuthTokenService` — emissão
  de token vira JWT no gateway.

**Repurpose (validação de credencial permanece como serviço de domínio):**
- `AutenticarUC` deixa de emitir token e passa a **validar credenciais e devolver a identidade**
  (`cpf`). A regra (BCrypt, "credenciais inválidas") continua no caso de uso/serviço de domínio.
- Exposto por um endpoint **interno** `POST /internal/auth/validar` (consumido só pelo gateway;
  não roteado publicamente).
- `Cadastro de Clientes` (UC11) permanece inteiro na pizzaria (`ClienteService`).

**Confiança no gateway:**
- Onde um endpoint precisar do usuário autenticado, lê o header `X-Cliente-Cpf`
  (injetado pelo gateway), na camada de Adaptadores/Apresentacao — nunca no domínio.

**Registro e porta:**
- Vira Eureka client, `spring.application.name=pizzaria-service`, porta interna **8081**,
  sem expor porta pública (só o gateway é público).

## 6. Fluxo de autenticação (sequência)

```
Cliente ──POST /auth {email,senha}──▶ Gateway
                                         │  WebClient
                                         ├──POST /internal/auth/validar──▶ Pizzaria (BCrypt)
                                         │◀──── {cpf,email} | 401 ────────┘
                                         │  emite JWT(cpf)
Cliente ◀──── {token,cpf,email} ─────────┘

Cliente ──GET /pedidos/1/status  (Bearer JWT)──▶ Gateway
                                                   │ valida JWT, injeta X-Cliente-Cpf
                                                   ├──────────────────────▶ Pizzaria
                                                   │◀──────── 200 ─────────┘
Cliente ◀──────────── 200 ────────────────────────┘
(sem/inválido token → 401 no gateway, não chega na pizzaria)
```

## 7. Conteinerização e execução

- Cada serviço com `Dockerfile` multistage (build Maven + runtime JRE 21).
- `docker-compose.yml` na raiz: sobe **eureka** primeiro (com healthcheck no `:8761`),
  depois **gateway** e **pizzaria** (que dependem do eureka via `depends_on`).
- Acesso público **somente** pelo gateway em `http://localhost:8080`; dashboard Eureka em `:8761`.
- Comando: `docker compose up --build`.
- Variáveis de ambiente: `JWT_SECRET`, `JWT_EXP_MIN`, e os endereços do Eureka.

## 8. Fora de escopo deste incremento

Microsserviço de **estoque** (BD próprio, JPA real), **entregas/RabbitMQ** (≥3 instâncias),
estratégias de **impostos** (env var) e **descontos** (endpoint), e os **novos UCs de
admin** (UC2 cardápio corrente, UC3/UC4 política de desconto). São os marcos de 17/06 e 22/06.

## 9. Validação

- **Descoberta:** o dashboard do Eureka (`:8761`) lista `gateway` e `pizzaria-service`.
- **Cadastro:** `POST /clientes` (via gateway) cria cliente — 201.
- **Login:** `POST /auth` (via gateway) retorna um JWT válido.
- **Rota protegida:** com `Bearer <jwt>` → 200; sem token / token inválido → 401 (barrado no gateway).
- **Auth fora da pizzaria:** chamada direta a `/internal/auth/validar` só é alcançável internamente;
  o pizzaria não tem mais filtro de token próprio.
- `test_api.py` ajustado para apontar ao gateway (`:8080`) roda verde.

## 10. Riscos

1. **Compatibilidade Boot 3.5 ↔ Spring Cloud** — fixar o trem 2024.0.x e validar cedo.
2. **Gateway reativo (WebFlux)** — a chamada de credencial usa `WebClient` (não `RestTemplate`);
   atenção para não bloquear a thread reativa.
3. **`git mv` do monólito** — ajustar caminhos do Dockerfile/compose e do `test_api.py`.
4. **Ordem de boot** — gateway/pizzaria devem tolerar Eureka ainda subindo (retry de registro).
