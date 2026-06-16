# Eureka + Gateway + Auth — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Colocar o monólito da Parte 1 atrás de um Spring Cloud Gateway (com autenticação JWT) registrado num Eureka name server, tudo conteinerizado.

**Architecture:** Três apps Spring Boot independentes na raiz — `eureka-server/` (name server), `gateway/` (entrada única + auth JWT) e `pizzaria-service/` (o monólito, sem auth, registrado no Eureka). A autenticação sai do serviço principal: o gateway valida credenciais chamando um endpoint interno da pizzaria, emite JWT e injeta a identidade (`X-Cliente-Cpf`) no downstream. Clean Architecture do pizzaria permanece intacta; o gateway também é organizado em camadas.

**Tech Stack:** Java 21, Spring Boot 3.5.4, Spring Cloud (trem compatível com Boot 3.5), Eureka, Spring Cloud Gateway (WebFlux), jjwt (HS256), Docker Compose.

> ⚠️ **JDK 21 obrigatório.** Antes de qualquer Maven: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`.

---

## File Structure

```
Projarc/
├── docker-compose.yml          # MODIFICAR: orquestra eureka + gateway + pizzaria
├── eureka-server/              # CRIAR
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/bcopstein/eureka/EurekaServerApplication.java
│   └── src/main/resources/application.yml
├── gateway/                    # CRIAR
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/java/com/bcopstein/gateway/
│   │   ├── GatewayApplication.java
│   │   ├── config/WebClientConfig.java
│   │   ├── apresentacao/AuthController.java
│   │   ├── apresentacao/AuthGlobalFilter.java
│   │   ├── apresentacao/RotasPublicas.java
│   │   ├── aplicacao/LoginUC.java
│   │   ├── aplicacao/LoginRequest.java
│   │   ├── aplicacao/TokenResponse.java
│   │   ├── servicos/ITokenService.java
│   │   ├── servicos/JwtTokenService.java
│   │   ├── servicos/ICredenciaisService.java
│   │   ├── servicos/PizzariaCredenciaisService.java
│   │   ├── servicos/Identidade.java
│   │   └── servicos/CredenciaisInvalidasException.java
│   └── src/main/resources/application.yml
│   └── src/test/java/com/bcopstein/gateway/...
├── pizzaria-service/           # MOVER (git mv do monólito atual)
│   └── (pom.xml, mvnw, .mvn, src, Dockerfile, .dockerignore)
└── (raiz mantém: docs/, *.puml, *.pdf, README.md, CLAUDE.md, enunciado.md, insomnia.json, test_api.py)
```

---

## Task 1: Mover o monólito para `pizzaria-service/`

**Files:**
- Move: `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `src/`, `Dockerfile`, `.dockerignore` → `pizzaria-service/`

- [ ] **Step 1: Criar a pasta e mover os arquivos do projeto Maven (preservando histórico)**

```bash
mkdir -p pizzaria-service
git mv pom.xml mvnw mvnw.cmd .mvn src Dockerfile .dockerignore pizzaria-service/
```

- [ ] **Step 2: Verificar que o monólito ainda compila no novo local**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd pizzaria-service && ./mvnw -q -DskipTests compile && cd ..
```
Expected: build sem erros (exit 0).

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "refactor(t2): move o monólito para pizzaria-service/"
```

---

## Task 2: Criar o Eureka Server (name server)

**Files:**
- Create: `eureka-server/pom.xml`, `eureka-server/src/main/java/com/bcopstein/eureka/EurekaServerApplication.java`, `eureka-server/src/main/resources/application.yml`

- [ ] **Step 1: Criar `eureka-server/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.4</version>
    <relativePath/>
  </parent>
  <groupId>com.bcopstein</groupId>
  <artifactId>eureka-server</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>21</java.version>
    <spring-cloud.version>2025.0.0</spring-cloud.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
  </dependencies>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring-cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

> Se o build falhar com erro de BOM, ajustar `spring-cloud.version` para o trem compatível com Boot 3.5 (família 2025.0.x). Esse é o risco técnico nº 1 do spec — resolver aqui antes de seguir.

- [ ] **Step 2: Criar `EurekaServerApplication.java`**

```java
package com.bcopstein.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

- [ ] **Step 3: Criar `application.yml`**

```yaml
server:
  port: 8761
spring:
  application:
    name: eureka-server
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  instance:
    hostname: ${EUREKA_HOST:localhost}
```

- [ ] **Step 4: Verificar que o Eureka sobe e o dashboard responde**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd eureka-server && ./mvnw -q spring-boot:run &
sleep 25 && curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8761/
```
Expected: `200`. Encerrar com `kill %1` depois.

> Se não houver `mvnw` na pasta, copie o wrapper: `cp -r ../pizzaria-service/.mvn ../pizzaria-service/mvnw ../pizzaria-service/mvnw.cmd .` (cada serviço pode usar o mesmo wrapper).

- [ ] **Step 5: Commit**

```bash
git add eureka-server
git commit -m "feat(t2): adiciona Eureka name server"
```

---

## Task 3: Pizzaria — vira Eureka client e retira a autenticação

**Files:**
- Modify: `pizzaria-service/pom.xml` (deps eureka client + cloud BOM)
- Modify: `pizzaria-service/src/main/resources/application.yaml` (nome, porta, eureka)
- Create: `.../Aplicacao/Responses/ValidacaoCredenciaisResponse.java`
- Modify: `.../Aplicacao/AutenticarUC.java`
- Create: `.../Adaptadores/Apresentacao/InternalAuthController.java`
- Delete: `.../Adaptadores/Apresentacao/AuthFilter.java`, `.../Adaptadores/Apresentacao/AuthController.java`, `.../Adaptadores/Servicos/AuthTokenService.java`, `.../Dominio/Servicos/IAuthTokenService.java`, `.../Aplicacao/Responses/AuthResponse.java`
- Test: `.../Aplicacao/AutenticarUCTest.java`

- [ ] **Step 1: Adicionar dependências de Eureka client + BOM no `pizzaria-service/pom.xml`**

Adicionar à seção `<properties>`:
```xml
    <spring-cloud.version>2025.0.0</spring-cloud.version>
```
Adicionar em `<dependencies>`:
```xml
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
```
Adicionar (ou completar) `<dependencyManagement>`:
```xml
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring-cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
```

- [ ] **Step 2: Ajustar `application.yaml` (nome do serviço, porta interna, eureka)**

Trocar o bloco `application.name` e `server.port`, e adicionar `eureka`:
```yaml
spring:
  application:
    name: pizzaria-service
server:
  port: 8081
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URI:http://localhost:8761/eureka}
```
(Manter o restante do arquivo — datasource, jpa, h2, springdoc — como está.)

- [ ] **Step 3: Criar o DTO de resposta da validação de credenciais**

`.../Aplicacao/Responses/ValidacaoCredenciaisResponse.java`:
```java
package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses;

public record ValidacaoCredenciaisResponse(String cpf, String email) {}
```

- [ ] **Step 4: Escrever o teste do `AutenticarUC` (sem token, devolvendo identidade)**

`.../Aplicacao/AutenticarUCTest.java`:
```java
package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ValidacaoCredenciaisResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ClienteService;

class AutenticarUCTest {

    @Test
    void devolveIdentidadeQuandoCredenciaisValidas() {
        ClienteService clienteService = mock(ClienteService.class);
        String hash = new BCryptPasswordEncoder().encode("senha");
        Cliente c = new Cliente("9001", "Huguinho", "999", "Rua A", "hug@email.com", hash);
        when(clienteService.recuperaPorEmail("hug@email.com")).thenReturn(Optional.of(c));

        AutenticarUC uc = new AutenticarUC(clienteService);
        ValidacaoCredenciaisResponse resp = uc.run(new LoginRequest("hug@email.com", "senha"));

        assertEquals("9001", resp.cpf());
        assertEquals("hug@email.com", resp.email());
    }

    @Test
    void lancaQuandoSenhaErrada() {
        ClienteService clienteService = mock(ClienteService.class);
        String hash = new BCryptPasswordEncoder().encode("senha");
        Cliente c = new Cliente("9001", "Huguinho", "999", "Rua A", "hug@email.com", hash);
        when(clienteService.recuperaPorEmail("hug@email.com")).thenReturn(Optional.of(c));

        AutenticarUC uc = new AutenticarUC(clienteService);
        assertThrows(IllegalArgumentException.class,
                () -> uc.run(new LoginRequest("hug@email.com", "errada")));
    }
}
```

> Conferir o construtor real de `Cliente` e o `LoginRequest` (ordem dos campos) ao executar; ajustar o teste se a assinatura diferir.

- [ ] **Step 5: Rodar o teste e ver falhar**

```bash
cd pizzaria-service && ./mvnw -q -Dtest=AutenticarUCTest test
```
Expected: FALHA na compilação/asserção (o `AutenticarUC` ainda devolve `AuthResponse` com token).

- [ ] **Step 6: Reescrever `AutenticarUC` (remover token, devolver identidade)**

`.../Aplicacao/AutenticarUC.java`:
```java
package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ValidacaoCredenciaisResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ClienteService;

/**
 * UC de validação de credenciais (a emissão de token foi para o gateway).
 * Confere email/senha e devolve a identidade (cpf) do cliente.
 */
@Service
@Transactional
public class AutenticarUC {

    private final ClienteService clienteService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AutenticarUC(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public ValidacaoCredenciaisResponse run(LoginRequest req) {
        Cliente c = clienteService.recuperaPorEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        String stored = c.getSenha();
        boolean ok = (stored != null && stored.startsWith("$2"))
                ? passwordEncoder.matches(req.getSenha(), stored)
                : req.getSenha().equals(stored);

        if (!ok) throw new IllegalArgumentException("Credenciais inválidas");

        return new ValidacaoCredenciaisResponse(c.getCpf(), c.getEmail());
    }
}
```

- [ ] **Step 7: Criar o `InternalAuthController` (endpoint interno consumido pelo gateway)**

`.../Adaptadores/Apresentacao/InternalAuthController.java`:
```java
package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.AutenticarUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ValidacaoCredenciaisResponse;

import jakarta.validation.Valid;

/**
 * Endpoint INTERNO: valida credenciais para o gateway (que emite o JWT).
 * Não é destinado ao cliente final — o gateway é quem consome.
 */
@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final AutenticarUC autenticarUC;

    public InternalAuthController(AutenticarUC autenticarUC) {
        this.autenticarUC = autenticarUC;
    }

    @PostMapping("/validar")
    public ResponseEntity<ValidacaoCredenciaisResponse> validar(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(autenticarUC.run(req));
    }
}
```

> O `GlobalExceptionHandler` existente já deve mapear `IllegalArgumentException` para 4xx; garantir que credencial inválida vire **401**. Se hoje devolve 400, adicionar um tratamento para devolver 401 nesse caminho (o gateway distingue 401 de 200).

- [ ] **Step 8: Apagar os artefatos de autenticação que saíram do serviço principal**

```bash
cd pizzaria-service
git rm src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Adaptadores/Apresentacao/AuthFilter.java \
       src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Adaptadores/Apresentacao/AuthController.java \
       src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Adaptadores/Servicos/AuthTokenService.java \
       src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Dominio/Servicos/IAuthTokenService.java \
       src/main/java/com/bcopstein/ex4_lancheriaddd_v1/Aplicacao/Responses/AuthResponse.java
cd ..
```

- [ ] **Step 9: Rodar os testes do pizzaria (o novo passa, o context-load valida o wiring)**

```bash
cd pizzaria-service && ./mvnw -q test && cd ..
```
Expected: PASS (incluindo `AutenticarUCTest` e o context-load).

> Se algum import quebrar (ex.: `AuthController` referenciava algo), corrigir. Conferir que nenhum arquivo ainda importa `IAuthTokenService`/`AuthResponse`: `grep -rn "IAuthTokenService\|AuthResponse" src` deve voltar vazio.

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat(t2): pizzaria vira eureka client e remove a autenticação (movida pro gateway)"
```

---

## Task 4: Esqueleto do Gateway (projeto + Eureka client + rotas)

**Files:**
- Create: `gateway/pom.xml`, `gateway/src/main/java/com/bcopstein/gateway/GatewayApplication.java`, `gateway/src/main/resources/application.yml`, `gateway/src/main/java/com/bcopstein/gateway/config/WebClientConfig.java`

- [ ] **Step 1: Criar `gateway/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.4</version>
    <relativePath/>
  </parent>
  <groupId>com.bcopstein</groupId>
  <artifactId>gateway</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>21</java.version>
    <spring-cloud.version>2025.0.0</spring-cloud.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.12.6</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>0.12.6</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>0.12.6</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>io.projectreactor</groupId>
      <artifactId>reactor-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring-cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 2: Criar `GatewayApplication.java`**

```java
package com.bcopstein.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

- [ ] **Step 3: Criar `config/WebClientConfig.java` (WebClient balanceado para falar com a pizzaria)**

```java
package com.bcopstein.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

- [ ] **Step 4: Criar `application.yml`**

```yaml
server:
  port: 8080
spring:
  application:
    name: gateway
  cloud:
    gateway:
      routes:
        - id: pizzaria
          uri: lb://pizzaria-service
          predicates:
            - Path=/clientes/**,/cardapio/**,/pedidos/**,/docs/**,/swagger-ui/**,/v3/api-docs/**
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URI:http://localhost:8761/eureka}
jwt:
  secret: ${JWT_SECRET:troque-esta-chave-secreta-de-no-minimo-32-bytes!!}
  expiracao-min: ${JWT_EXP_MIN:480}
```

- [ ] **Step 5: Verificar que o gateway sobe (com Eureka já rodando da Task 2)**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd gateway && ./mvnw -q -DskipTests compile && cd ..
```
Expected: build sem erros. (Cópia do wrapper como na Task 2, Step 4, se necessário.)

- [ ] **Step 6: Commit**

```bash
git add gateway
git commit -m "feat(t2): esqueleto do gateway (rotas + eureka client + webclient)"
```

---

## Task 5: Serviço de token JWT no gateway (TDD)

**Files:**
- Create: `gateway/src/main/java/com/bcopstein/gateway/servicos/ITokenService.java`, `.../servicos/JwtTokenService.java`
- Test: `gateway/src/test/java/com/bcopstein/gateway/servicos/JwtTokenServiceTest.java`

- [ ] **Step 1: Criar a interface `ITokenService`**

```java
package com.bcopstein.gateway.servicos;

public interface ITokenService {
    String gerar(String cpf);
    boolean valido(String token);
    String cpfDe(String token);
}
```

- [ ] **Step 2: Escrever o teste `JwtTokenServiceTest`**

```java
package com.bcopstein.gateway.servicos;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private final JwtTokenService service =
            new JwtTokenService("uma-chave-secreta-bem-grande-para-hs256-1234", 480);

    @Test
    void gerarEValidarTokenComCpf() {
        String token = service.gerar("9001");
        assertTrue(service.valido(token));
        assertEquals("9001", service.cpfDe(token));
    }

    @Test
    void tokenAdulteradoEhInvalido() {
        String token = service.gerar("9001");
        String adulterado = token.substring(0, token.length() - 2) + "xx";
        assertFalse(service.valido(adulterado));
    }

    @Test
    void tokenDeOutraChaveEhInvalido() {
        JwtTokenService outro =
                new JwtTokenService("uma-chave-secreta-completamente-diferente-9999", 480);
        String token = outro.gerar("9001");
        assertFalse(service.valido(token));
    }
}
```

- [ ] **Step 3: Rodar o teste e ver falhar**

```bash
cd gateway && ./mvnw -q -Dtest=JwtTokenServiceTest test
```
Expected: FALHA de compilação (`JwtTokenService` ainda não existe).

- [ ] **Step 4: Implementar `JwtTokenService`**

```java
package com.bcopstein.gateway.servicos;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService implements ITokenService {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiracao-min}") long expiracaoMin) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMin * 60_000L;
    }

    @Override
    public String gerar(String cpf) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(cpf)
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracaoMs))
                .signWith(chave)
                .compact();
    }

    @Override
    public boolean valido(String token) {
        try {
            Jwts.parser().verifyWith(chave).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String cpfDe(String token) {
        return Jwts.parser().verifyWith(chave).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
}
```

- [ ] **Step 5: Rodar o teste e ver passar**

```bash
cd gateway && ./mvnw -q -Dtest=JwtTokenServiceTest test
```
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add gateway
git commit -m "feat(t2): serviço de token JWT (HS256) no gateway"
```

---

## Task 6: Cliente de credenciais (gateway → pizzaria)

**Files:**
- Create: `.../servicos/Identidade.java`, `.../servicos/CredenciaisInvalidasException.java`, `.../servicos/ICredenciaisService.java`, `.../servicos/PizzariaCredenciaisService.java`

- [ ] **Step 1: Criar os tipos de apoio**

`.../servicos/Identidade.java`:
```java
package com.bcopstein.gateway.servicos;

public record Identidade(String cpf, String email) {}
```

`.../servicos/CredenciaisInvalidasException.java`:
```java
package com.bcopstein.gateway.servicos;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}
```

- [ ] **Step 2: Criar a interface `ICredenciaisService`**

```java
package com.bcopstein.gateway.servicos;

import reactor.core.publisher.Mono;

public interface ICredenciaisService {
    Mono<Identidade> validar(String email, String senha);
}
```

- [ ] **Step 3: Implementar `PizzariaCredenciaisService` (WebClient reativo)**

```java
package com.bcopstein.gateway.servicos;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class PizzariaCredenciaisService implements ICredenciaisService {

    private final WebClient webClient;

    public PizzariaCredenciaisService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("lb://pizzaria-service").build();
    }

    @Override
    public Mono<Identidade> validar(String email, String senha) {
        return webClient.post()
                .uri("/internal/auth/validar")
                .bodyValue(Map.of("email", email, "senha", senha))
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 400,
                          resp -> Mono.error(new CredenciaisInvalidasException()))
                .bodyToMono(Identidade.class);
    }
}
```

> A pizzaria devolve `{cpf, email}` em `/internal/auth/validar` — o record `Identidade` casa com esse JSON.

- [ ] **Step 4: Compilar**

```bash
cd gateway && ./mvnw -q -DskipTests compile && cd ..
```
Expected: build sem erros.

- [ ] **Step 5: Commit**

```bash
git add gateway
git commit -m "feat(t2): cliente de credenciais do gateway para a pizzaria"
```

---

## Task 7: Login no gateway — UC + controller (TDD do UC)

**Files:**
- Create: `.../aplicacao/LoginRequest.java`, `.../aplicacao/TokenResponse.java`, `.../aplicacao/LoginUC.java`, `.../apresentacao/AuthController.java`
- Test: `gateway/src/test/java/com/bcopstein/gateway/aplicacao/LoginUCTest.java`

- [ ] **Step 1: Criar os DTOs**

`.../aplicacao/LoginRequest.java`:
```java
package com.bcopstein.gateway.aplicacao;

public record LoginRequest(String email, String senha) {}
```

`.../aplicacao/TokenResponse.java`:
```java
package com.bcopstein.gateway.aplicacao;

public record TokenResponse(String token, String cpf, String email) {}
```

- [ ] **Step 2: Escrever o teste `LoginUCTest`**

```java
package com.bcopstein.gateway.aplicacao;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import com.bcopstein.gateway.servicos.CredenciaisInvalidasException;
import com.bcopstein.gateway.servicos.ICredenciaisService;
import com.bcopstein.gateway.servicos.ITokenService;
import com.bcopstein.gateway.servicos.Identidade;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LoginUCTest {

    @Test
    void emiteTokenQuandoCredenciaisValidas() {
        ICredenciaisService cred = mock(ICredenciaisService.class);
        ITokenService tokens = mock(ITokenService.class);
        when(cred.validar("hug@email.com", "senha"))
                .thenReturn(Mono.just(new Identidade("9001", "hug@email.com")));
        when(tokens.gerar("9001")).thenReturn("jwt-fake");

        LoginUC uc = new LoginUC(cred, tokens);

        StepVerifier.create(uc.run("hug@email.com", "senha"))
                .expectNextMatches(r -> r.token().equals("jwt-fake")
                        && r.cpf().equals("9001")
                        && r.email().equals("hug@email.com"))
                .verifyComplete();
    }

    @Test
    void propagaErroQuandoCredenciaisInvalidas() {
        ICredenciaisService cred = mock(ICredenciaisService.class);
        ITokenService tokens = mock(ITokenService.class);
        when(cred.validar(anyString(), anyString()))
                .thenReturn(Mono.error(new CredenciaisInvalidasException()));

        LoginUC uc = new LoginUC(cred, tokens);

        StepVerifier.create(uc.run("x@x.com", "errada"))
                .expectError(CredenciaisInvalidasException.class)
                .verify();
    }
}
```

- [ ] **Step 3: Rodar o teste e ver falhar**

```bash
cd gateway && ./mvnw -q -Dtest=LoginUCTest test
```
Expected: FALHA de compilação (`LoginUC` ainda não existe).

- [ ] **Step 4: Implementar `LoginUC`**

```java
package com.bcopstein.gateway.aplicacao;

import org.springframework.stereotype.Service;

import com.bcopstein.gateway.servicos.ICredenciaisService;
import com.bcopstein.gateway.servicos.ITokenService;

import reactor.core.publisher.Mono;

@Service
public class LoginUC {

    private final ICredenciaisService credenciais;
    private final ITokenService tokens;

    public LoginUC(ICredenciaisService credenciais, ITokenService tokens) {
        this.credenciais = credenciais;
        this.tokens = tokens;
    }

    public Mono<TokenResponse> run(String email, String senha) {
        return credenciais.validar(email, senha)
                .map(id -> new TokenResponse(tokens.gerar(id.cpf()), id.cpf(), id.email()));
    }
}
```

- [ ] **Step 5: Rodar o teste e ver passar**

```bash
cd gateway && ./mvnw -q -Dtest=LoginUCTest test
```
Expected: PASS.

- [ ] **Step 6: Implementar o `AuthController` (POST /auth, reativo)**

`.../apresentacao/AuthController.java`:
```java
package com.bcopstein.gateway.apresentacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.gateway.aplicacao.LoginRequest;
import com.bcopstein.gateway.aplicacao.LoginUC;
import com.bcopstein.gateway.aplicacao.TokenResponse;
import com.bcopstein.gateway.servicos.CredenciaisInvalidasException;

import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    private final LoginUC loginUC;

    public AuthController(LoginUC loginUC) {
        this.loginUC = loginUC;
    }

    @PostMapping("/auth")
    public Mono<ResponseEntity<TokenResponse>> auth(@RequestBody LoginRequest req) {
        return loginUC.run(req.email(), req.senha())
                .map(ResponseEntity::ok)
                .onErrorResume(CredenciaisInvalidasException.class,
                        e -> Mono.just(ResponseEntity.status(401).build()));
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add gateway
git commit -m "feat(t2): login (UC12) no gateway emitindo JWT"
```

---

## Task 8: Filtro global de autenticação JWT no gateway

**Files:**
- Create: `.../apresentacao/RotasPublicas.java`, `.../apresentacao/AuthGlobalFilter.java`
- Test: `gateway/src/test/java/com/bcopstein/gateway/apresentacao/RotasPublicasTest.java`

- [ ] **Step 1: Escrever o teste de `RotasPublicas` (decisão de rota aberta/protegida)**

```java
package com.bcopstein.gateway.apresentacao;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class RotasPublicasTest {

    @Test
    void cadastroEloginSaoPublicos() {
        assertTrue(RotasPublicas.ehPublica(HttpMethod.POST, "/clientes"));
        assertTrue(RotasPublicas.ehPublica(HttpMethod.POST, "/auth"));
        assertTrue(RotasPublicas.ehPublica(HttpMethod.GET, "/docs"));
    }

    @Test
    void rotasDePedidoSaoProtegidas() {
        assertFalse(RotasPublicas.ehPublica(HttpMethod.POST, "/pedidos"));
        assertFalse(RotasPublicas.ehPublica(HttpMethod.GET, "/pedidos/1/status"));
        assertFalse(RotasPublicas.ehPublica(HttpMethod.GET, "/cardapio/1"));
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

```bash
cd gateway && ./mvnw -q -Dtest=RotasPublicasTest test
```
Expected: FALHA de compilação (`RotasPublicas` não existe).

- [ ] **Step 3: Implementar `RotasPublicas`**

```java
package com.bcopstein.gateway.apresentacao;

import org.springframework.http.HttpMethod;

/** Decide quais rotas dispensam JWT. */
public final class RotasPublicas {
    private RotasPublicas() {}

    public static boolean ehPublica(HttpMethod metodo, String path) {
        if (HttpMethod.POST.equals(metodo) && path.equals("/auth")) return true;
        if (HttpMethod.POST.equals(metodo) && path.equals("/clientes")) return true;
        if (path.startsWith("/docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) return true;
        return false;
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

```bash
cd gateway && ./mvnw -q -Dtest=RotasPublicasTest test
```
Expected: PASS.

- [ ] **Step 5: Implementar o `AuthGlobalFilter`**

```java
package com.bcopstein.gateway.apresentacao;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.bcopstein.gateway.servicos.ITokenService;

import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final ITokenService tokens;

    public AuthGlobalFilter(ITokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();

        if (RotasPublicas.ehPublica(req.getMethod(), req.getPath().value())) {
            return chain.filter(exchange);
        }

        String header = req.getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return naoAutorizado(exchange, "Token ausente.");
        }

        String token = header.substring("Bearer ".length()).trim();
        if (!tokens.valido(token)) {
            return naoAutorizado(exchange, "Token inválido ou expirado.");
        }

        ServerHttpRequest mutado = req.mutate()
                .header("X-Cliente-Cpf", tokens.cpfDe(token))
                .build();
        return chain.filter(exchange.mutate().request(mutado).build());
    }

    private Mono<Void> naoAutorizado(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(("{\"erro\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // antes do roteamento
    }
}
```

- [ ] **Step 6: Build completo do gateway**

```bash
cd gateway && ./mvnw -q test && cd ..
```
Expected: PASS (todos os testes do gateway).

- [ ] **Step 7: Commit**

```bash
git add gateway
git commit -m "feat(t2): filtro global de autenticação JWT no gateway"
```

---

## Task 9: Conteinerização e docker-compose (orquestração)

**Files:**
- Create: `eureka-server/Dockerfile`, `gateway/Dockerfile`
- Modify: `docker-compose.yml` (raiz)
- (pizzaria-service/Dockerfile já existe, movido na Task 1 — ajustar EXPOSE para 8081)

- [ ] **Step 1: Dockerfile do Eureka** (`eureka-server/Dockerfile`)

```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress -q
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","app.jar"]
```

- [ ] **Step 2: Dockerfile do Gateway** (`gateway/Dockerfile`) — igual ao do Eureka, trocando `EXPOSE 8761` por `EXPOSE 8080`.

```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress -q
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

- [ ] **Step 3: Ajustar o `EXPOSE` do `pizzaria-service/Dockerfile` para `8081`**

Trocar a linha `EXPOSE 8080` por `EXPOSE 8081`.

- [ ] **Step 4: Reescrever o `docker-compose.yml` da raiz**

```yaml
services:
  eureka-server:
    build: ./eureka-server
    container_name: eureka-server
    ports:
      - "8761:8761"
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:8761/ || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 20s

  pizzaria-service:
    build: ./pizzaria-service
    container_name: pizzaria-service
    environment:
      EUREKA_URI: http://eureka-server:8761/eureka
    expose:
      - "8081"
    depends_on:
      eureka-server:
        condition: service_healthy

  gateway:
    build: ./gateway
    container_name: gateway
    ports:
      - "8080:8080"
    environment:
      EUREKA_URI: http://eureka-server:8761/eureka
      JWT_SECRET: troque-esta-chave-secreta-de-no-minimo-32-bytes!!
      JWT_EXP_MIN: "480"
    depends_on:
      eureka-server:
        condition: service_healthy
```

- [ ] **Step 5: Subir tudo e validar a descoberta**

```bash
docker compose up --build -d
sleep 60
curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" | grep -o '"name":"[^"]*"'
```
Expected: aparecem `GATEWAY` e `PIZZARIA-SERVICE` registrados.

- [ ] **Step 6: Validar o fluxo end-to-end via gateway**

```bash
# cadastro (rota aberta)
curl -s -o /dev/null -w "cadastro=%{http_code}\n" -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"cpf":"9001","nome":"Huguinho","celular":"999","endereco":"Rua A","email":"hug@email.com","senha":"senha"}'

# login (gateway emite JWT)
TOKEN=$(curl -s -X POST http://localhost:8080/auth \
  -H "Content-Type: application/json" \
  -d '{"email":"hug@email.com","senha":"senha"}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
echo "TOKEN=$TOKEN"

# rota protegida sem token -> 401
curl -s -o /dev/null -w "sem_token=%{http_code}\n" http://localhost:8080/cardapio/1

# rota protegida com token -> 200
curl -s -o /dev/null -w "com_token=%{http_code}\n" http://localhost:8080/cardapio/1 \
  -H "Authorization: Bearer $TOKEN"
```
Expected: `cadastro=201`, `TOKEN` não vazio, `sem_token=401`, `com_token=200`.

- [ ] **Step 7: Derrubar e commit**

```bash
docker compose down
git add docker-compose.yml eureka-server/Dockerfile gateway/Dockerfile pizzaria-service/Dockerfile
git commit -m "feat(t2): conteineriza e orquestra eureka + gateway + pizzaria"
```

---

## Task 10: Ajustar `test_api.py` e README para o gateway

**Files:**
- Modify: `test_api.py` (base URL → gateway :8080; login agora devolve JWT)
- Modify: `README.md` (instruções de execução via docker compose + portas)

- [ ] **Step 1: Apontar o `test_api.py` para o gateway**

Trocar a base URL para `http://localhost:8080` e garantir que o passo de login use `POST /auth` (resposta `{token,...}`) e mande `Authorization: Bearer <token>` nas rotas protegidas. (O fluxo de endpoints é o mesmo; muda só o host único e o fato de o token vir do gateway.)

- [ ] **Step 2: Rodar o smoke test contra o ambiente em pé**

```bash
docker compose up --build -d && sleep 60
python3 test_api.py
docker compose down
```
Expected: cenários de cadastro/login/rotas protegidas verdes via gateway.

- [ ] **Step 3: Atualizar a seção de execução do README**

Documentar: `docker compose up --build`; gateway em `:8080` (única porta pública), Eureka em `:8761`; login em `POST /auth` devolve JWT; rotas protegidas exigem `Authorization: Bearer <jwt>`.

- [ ] **Step 4: Commit**

```bash
git add test_api.py README.md
git commit -m "docs(t2): aponta test_api e README para o gateway"
```

---

## Self-Review (preenchido)

**Cobertura do spec:**
- §3 estrutura de pastas → Task 1, 2, 4. ✅
- §5.1 Eureka → Task 2. ✅
- §5.2 Gateway (rotas, login, filtro, camadas) → Tasks 4–8. ✅
- §5.3 pizzaria (eureka client, remoção de auth, endpoint interno, header de identidade) → Task 3. ✅
- §6 fluxo de auth → Tasks 6–8 + verificação Task 9 Step 6. ✅
- §7 conteinerização/compose → Task 9. ✅
- §9 validação → Task 9 Steps 5–6 + Task 10. ✅
- §2 princípios de arquitetura: Clean Arch do pizzaria intacta (auth só na borda; domínio não muda); gateway em camadas (apresentacao/aplicacao/servicos) com serviços atrás de interface. ✅

**Riscos lembrados no plano:** compatibilidade Boot↔Cloud (Task 2 Step 1), blocking reativo (resolvido com `Mono`/WebClient), `git mv` de caminhos (Task 1), ordem de boot (healthcheck + depends_on na Task 9).

**Pendência consciente:** confirmar a versão exata do trem Spring Cloud para Boot 3.5 ao rodar a Task 2 (ajustar `2025.0.0` se o BOM não resolver).
