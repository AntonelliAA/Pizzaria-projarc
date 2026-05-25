# PAS – Trabalho Final 2026/1 — Pizzaria (Parte 1)

> **Disciplina:** Projeto e Arquitetura de Software  
> **Período:** 2026/1  
> **Tema:** Sistema de Tele-Entrega para Pizzaria

---

## Enunciado Geral

Uma pizzaria de tele-entrega deseja disponibilizar um sistema de pedidos online. O **cliente** acessa o sistema usando um aplicativo no celular. Inicialmente o cliente deve entrar no aplicativo. Se ainda não tiver cadastro, deve se cadastrar.

### Cadastro do Cliente
O cadastro compreende os seguintes dados:

| Campo    | Tipo       |
|----------|------------|
| Nome     | Texto      |
| CPF      | Texto      |
| Celular  | Texto      |
| Endereço | Texto      |
| E-mail   | Texto (login) |
| Senha    | Texto      |

> O **"usuário"** será sempre o **e-mail**.

---

## Fluxo do Pedido

### 1. Montagem do Pedido
Depois de entrar no sistema, o cliente pode:
- Consultar o **cardápio**
- Visualizar a descrição de cada item e seu **preço unitário**
- Montar um pedido com **quantos itens desejar**, indicando a **quantidade** de cada um
- Informar o **endereço de entrega** ao finalizar

### 2. Submissão para Aprovação (`status: NOVO`)
Quando o pedido está completo, o cliente o submete para aprovação:

1. O sistema verifica no **estoque** se há ingredientes suficientes para todos os itens.
2. **Se não houver ingredientes:**
   - O pedido é retornado destacando os itens que **não podem ser atendidos**
   - Os itens correspondentes no cardápio são automaticamente marcados como **indisponíveis** (até a reposição do estoque)
3. **Se tudo estiver ok:**
   - O status é alterado para **`APROVADO`**
   - O **custo do pedido** é calculado

### 3. Cálculo do Custo

```
Custo Final = (Soma dos itens − Desconto) + Imposto
```

| Componente | Regra |
|------------|-------|
| **Imposto** | 10% sobre o somatório do custo dos itens |
| **Desconto** | 7% no custo de cada item — para clientes com **mais de 3 pedidos nos últimos 20 dias** |

### 4. Pagamento (`status: PAGO`)
O pedido aprovado é retornado ao cliente, que pode:

- **Cancelar** o pedido → pedido abandonado
- **Pagar** → pedido é encaminhado para a **fila da cozinha** com status `PAGO` *(não pode mais ser cancelado)*; o cliente recebe o **número do pedido**

### 5. Cozinha

| Status        | Evento                          |
|---------------|---------------------------------|
| `AGUARDANDO`  | Pedido recebido na cozinha      |
| `PREPARAÇÃO`  | Pedido começou a ser preparado  |
| `PRONTO`      | Pedido finalizado → fila de entregas |

### 6. Entrega

| Status      | Evento                                 |
|-------------|----------------------------------------|
| `TRANSPORTE`| Entregador recebeu o pedido            |
| `ENTREGUE`  | Pedido foi entregue ao cliente         |

> Pedidos entregues são **arquivados** no sistema associados ao cliente.  
> As mudanças de status podem ser acompanhadas pelo app a partir do **número do pedido**.

---

## O Estudo de Caso

O enunciado foi trabalhado em aula na forma de um estudo de caso (slides e código disponíveis no Moodle).

O código apresentado implementa dois casos de uso relacionados ao **cardápio**:
1. Retorna a lista de cardápios disponíveis para que o gerente indique o cardápio corrente
2. Retorna o cardápio corrente com as anotações do "chef" para que os clientes montem seus pedidos

O código segue uma estrutura de pastas baseada em **arquitetura limpa (Clean Architecture)**, mantendo 3 níveis com isolamento adequado.

---

## Objetivos do Trabalho

Dar continuidade ao projeto do estudo de caso, implementando os seguintes **casos de uso** (🔒 = requer autenticação):

| UC   | Descrição |
|------|-----------|
| UC1  | **Registrar cliente** — O cliente se registra para poder fazer login futuramente |
| UC2  | **Autenticar** 🔒 — O cliente se autentica para operar no sistema |
| UC3  | **Carregar cardápio** 🔒 — Retorna sempre o cardápio de código 1 (prever controle do cardápio corrente) |
| UC4  | **Submeter pedido para aprovação** 🔒 — Retorna o pedido aprovado com preço calculado, ou negado por falta de ingredientes |
| UC5  | **Solicitar status de pedido** 🔒 |
| UC6  | **Cancelar pedido** 🔒 — Cancela pedido aprovado, mas não pago |
| UC7  | **Pagar pedido** 🔒 — Encaminha para cozinha; mudanças de estado são registradas; depois vai para entregas |
| UC8  | **Listar pedidos entregues entre duas datas** |
| UC9  | **Listar pedidos de um cliente entregues entre duas datas** |

---

## Serviços de Domínio

| Serviço           | Responsabilidade |
|-------------------|-----------------|
| **Cadastro de Clientes** | Manter o cadastro dos clientes |
| **Autenticação**  | Autenticação e autorização |
| **Pedidos**       | Verificar consistência, calcular valores; aciona Impostos e Descontos |
| **Estoque**       | Manter relação de itens disponíveis para preparo |
| **Cardápio**      | Manter lista de itens com preços e receitas |
| **Cozinha**       | Manter fila de pedidos e acompanhar preparo (versão simulada) |
| **Entrega**       | Manter fila de entregas, atribuir entregadores e acompanhar (versão simulada) |
| **Pagamento**     | Responsável pelos meios de pagamento |
| **Impostos**      | Cálculo de impostos |
| **Descontos**     | Políticas de descontos e fidelidade |

---

## Simplificações e Observações

- 🍕 O **serviço de cozinha** pode ser simulado (como no estudo de caso), mas o **status dos pedidos deve ser atualizado no banco de dados**
- 💳 O **serviço de pagamentos** pode ser um *fake* que responde sempre que o pagamento foi efetuado
- 📦 O **serviço de estoque** pode ser um *fake* que responde sempre que o estoque é suficiente
- 🚚 O **serviço de entregas** pode ser simulado como o de cozinha, incluindo a atualização de status no banco
- 🧮 **Impostos e descontos** devem ser projetados para **facilitar a mudança frequente nas fórmulas de cálculo**
- 🔄 Todos os serviços implementados como *fakes* ou simulados **devem ter interfaces previstas** para facilitar a troca futura por implementações definitivas

---

## Cronograma de Entregas

| Data       | Casos de Uso a Entregar            |
|------------|------------------------------------|
| 11/05/2026 | UC3 e UC4                          |
| 13/05/2026 | UC5 e UC6                          |
| 18/05/2026 | UC1, UC2 e UC7                     |
| 20/05/2026 | **Entrega Final** — UC8 e UC9      |

---

## Sobre o Desenvolvimento em Equipe

- A **participação de cada membro** será avaliada
- Todos devem ter número equilibrado de **commits** ou **pull-requests**
- Membros sem contribuições comprovadas **não terão nota atribuída**
- Definir o **líder do projeto**, responsável por criar o repositório base onde as versões serão consolidadas
