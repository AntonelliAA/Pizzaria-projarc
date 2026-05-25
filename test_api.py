#!/usr/bin/env python3
"""
=============================================================
  🍕 Pizzaria ECA — Central de Testes de API
=============================================================
  Testa todas as rotas e fluxos do sistema.

  Uso:
    python3 test_api.py                  # usa http://localhost:8080
    python3 test_api.py http://host:port # URL customizada

  Pré-requisito:
    pip3 install requests
=============================================================
"""

import sys
import json
import time
import requests

# ── Configuração ─────────────────────────────────────────────

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

# ── Cores para o terminal ────────────────────────────────────

class C:
    GREEN  = "\033[92m"
    RED    = "\033[91m"
    YELLOW = "\033[93m"
    CYAN   = "\033[96m"
    BOLD   = "\033[1m"
    DIM    = "\033[2m"
    RESET  = "\033[0m"

# ── Contadores ───────────────────────────────────────────────

total = 0
passed = 0
failed = 0
errors = []

# ── Helpers ──────────────────────────────────────────────────

def section(title: str):
    print(f"\n{C.BOLD}{C.CYAN}{'─' * 60}")
    print(f"  {title}")
    print(f"{'─' * 60}{C.RESET}\n")


def test(name: str, method: str, path: str, *,
         body: dict = None,
         expected_status: int = 200,
         checks: list = None,
         description: str = "",
         timeout: int = 10,
         token: str = None):
    """
    Executa um teste HTTP e valida o resultado.

    checks: lista de tuplas (descricao, funcao_lambda_sobre_json)
            ex: [("status é APROVADO", lambda j: j["status"] == "APROVADO")]
    token:  se informado, envia "Authorization: Bearer <token>"
    """
    global total, passed, failed

    total += 1
    url = f"{BASE_URL}{path}"
    prefix = f"  [{total:02d}] {name}"
    headers = {"Authorization": f"Bearer {token}"} if token else None

    try:
        if method == "GET":
            resp = requests.get(url, timeout=timeout, headers=headers)
        elif method == "POST":
            resp = requests.post(url, json=body, timeout=timeout, headers=headers)
        elif method == "PUT":
            resp = requests.put(url, json=body, timeout=timeout, headers=headers)
        elif method == "DELETE":
            resp = requests.delete(url, timeout=timeout, headers=headers)
        else:
            raise ValueError(f"Método HTTP desconhecido: {method}")

        # Verifica status code
        if resp.status_code != expected_status:
            failed += 1
            msg = (f"{prefix}\n"
                   f"       Esperado: HTTP {expected_status}\n"
                   f"       Recebido: HTTP {resp.status_code}\n"
                   f"       Body:     {resp.text[:300]}")
            errors.append(msg)
            print(f"{C.RED}  ✗ [{total:02d}] {name} — HTTP {resp.status_code} (esperava {expected_status}){C.RESET}")
            return None

        # Tenta parsear JSON (se houver body)
        data = None
        if resp.text.strip():
            try:
                data = resp.json()
            except Exception:
                data = resp.text

        # Executa checks adicionais
        if checks:
            for check_desc, check_fn in checks:
                if not check_fn(data):
                    failed += 1
                    msg = (f"{prefix}\n"
                           f"       Check falhou: {check_desc}\n"
                           f"       Body: {json.dumps(data, indent=2, ensure_ascii=False)[:300]}")
                    errors.append(msg)
                    print(f"{C.RED}  ✗ [{total:02d}] {name} — check falhou: {check_desc}{C.RESET}")
                    return data

        passed += 1
        status_info = f"HTTP {resp.status_code}"
        print(f"{C.GREEN}  ✓ [{total:02d}] {name} {C.DIM}({status_info}){C.RESET}")

        if description:
            print(f"{C.DIM}       ↳ {description}{C.RESET}")

        return data

    except requests.exceptions.ConnectionError:
        failed += 1
        msg = f"{prefix} — Não foi possível conectar em {url}"
        errors.append(msg)
        print(f"{C.RED}  ✗ [{total:02d}] {name} — CONEXÃO RECUSADA ({url}){C.RESET}")
        return None
    except Exception as e:
        failed += 1
        msg = f"{prefix} — Exceção: {e}"
        errors.append(msg)
        print(f"{C.RED}  ✗ [{total:02d}] {name} — ERRO: {e}{C.RESET}")
        return None


# =============================================================
#  TESTES
# =============================================================

def main():
    print(f"\n{C.BOLD}{C.CYAN}{'═' * 60}")
    print(f"  🍕  PIZZARIA ECA — CENTRAL DE TESTES")
    print(f"  Base URL: {BASE_URL}")
    print(f"{'═' * 60}{C.RESET}")

    # ─────────────────────────────────────────────────────────
    section("1. ROOT — Mensagem de boas-vindas")
    # ─────────────────────────────────────────────────────────

    test(
        "GET / — Welcome message",
        "GET", "/",
        checks=[
            ("Retorna texto de boas-vindas",
             lambda d: isinstance(d, str) and "Pizzaria" in d),
        ]
    )

    # ─────────────────────────────────────────────────────────
    section("2. UC1 — Registrar Cliente")
    # ─────────────────────────────────────────────────────────

    # Use timestamp to generate unique CPF/email for each test run
    ts = str(int(time.time()))[-6:]
    cpf_novo = f"T{ts}"
    email_novo = f"teste.{ts}@email.com"

    cliente_body = {
        "nome": "Teste Automatico",
        "cpf": cpf_novo,
        "celular": "51999999999",
        "endereco": "Rua Automática, 1",
        "email": email_novo,
        "senha": "senha123"
    }

    cliente_reg = test(
        "POST /clientes — Registrar novo cliente",
        "POST", "/clientes",
        body=cliente_body,
        expected_status=201,
        timeout=30,  # BCrypt pode ser lento na primeira chamada (JVM fria)
        checks=[
            ("Retorna o CPF do cliente", lambda d: d.get("cpf") == cpf_novo),
            ("Retorna o nome do cliente", lambda d: d.get("nome") == "Teste Automatico"),
        ],
        description="Cadastra um novo cliente com CPF único gerado por timestamp"
    )

    if cliente_reg:
        print(f"{C.DIM}       ↳ Cliente registrado: CPF={cliente_reg.get('cpf')}, "
              f"nome={cliente_reg.get('nome')}{C.RESET}")

    # Tentar registrar o mesmo CPF novamente → deve falhar com 400
    test(
        "POST /clientes — CPF duplicado (deve falhar com 400)",
        "POST", "/clientes",
        body=cliente_body,
        expected_status=400,
        timeout=30,
        description="Tentar cadastrar CPF já existente deve retornar 400"
    )

    # Dados inválidos (sem nome) → validação bean → 400
    test(
        "POST /clientes — Dados inválidos (sem nome) → 400",
        "POST", "/clientes",
        body={"cpf": "X999", "celular": "51988", "endereco": "X", "email": "x@y.com", "senha": "s"},
        expected_status=400,
        description="Body sem campo 'nome' deve retornar 400 (validação)"
    )

    # ─────────────────────────────────────────────────────────
    section("3. UC2 — Autenticar Cliente")
    # ─────────────────────────────────────────────────────────

    auth_resp = test(
        "POST /auth — Autenticar com credenciais válidas",
        "POST", "/auth",
        body={"email": email_novo, "senha": "senha123"},
        expected_status=200,
        checks=[
            ("Retorna token",  lambda d: d.get("token") is not None and len(d.get("token", "")) > 0),
            ("Retorna CPF",    lambda d: d.get("cpf") == cpf_novo),
            ("Retorna email",  lambda d: d.get("email") == email_novo),
        ],
        description="Deve retornar token, CPF e e-mail do cliente autenticado"
    )

    if auth_resp:
        print(f"{C.DIM}       ↳ Token: {auth_resp.get('token', '')[:20]}...{C.RESET}")

    # Autenticar com credenciais inválidas → 400
    test(
        "POST /auth — Senha incorreta → 400",
        "POST", "/auth",
        body={"email": email_novo, "senha": "senhaErrada"},
        expected_status=400,
        description="Senha incorreta deve retornar 400"
    )

    # Autenticar com e-mail inexistente → 400
    test(
        "POST /auth — E-mail inexistente → 400",
        "POST", "/auth",
        body={"email": "naoexiste@email.com", "senha": "qualquer"},
        expected_status=400,
        description="E-mail não cadastrado deve retornar 400"
    )

    # Autenticar clientes pré-existentes (data.sql) usando senha plain-text
    auth_9001 = test(
        "POST /auth — Autenticar cliente 9001 (huguinho.pato@email.com)",
        "POST", "/auth",
        body={"email": "huguinho.pato@email.com", "senha": "senha"},
        expected_status=200,
        checks=[
            ("Retorna token", lambda d: d.get("token") is not None),
            ("CPF correto",   lambda d: d.get("cpf") == "9001"),
        ],
        description="Cliente pré-existente com senha em plain-text deve autenticar"
    )

    auth_9002 = test(
        "POST /auth — Autenticar cliente 9002 (zezinho.pato@email.com)",
        "POST", "/auth",
        body={"email": "zezinho.pato@email.com", "senha": "senha"},
        expected_status=200,
        checks=[
            ("Retorna token", lambda d: d.get("token") is not None),
            ("CPF correto",   lambda d: d.get("cpf") == "9002"),
        ],
        description="Segundo cliente pré-existente deve autenticar"
    )

    # Token de sessão usado em todos os endpoints protegidos (UC3–UC7)
    TOKEN = auth_9001["token"] if auth_9001 else None

    # ─────────────────────────────────────────────────────────
    section("4. CARDÁPIO — Listar e recuperar (UC3)")
    # ─────────────────────────────────────────────────────────

    # Sanity checks de autenticação no cardápio
    test(
        "GET /cardapio/1 — Sem token → 401",
        "GET", "/cardapio/1",
        expected_status=401,
        description="Endpoint protegido deve recusar requisição sem Bearer token"
    )

    test(
        "GET /cardapio/1 — Token inválido → 401",
        "GET", "/cardapio/1",
        expected_status=401,
        token="token-falsificado-zzz",
        description="Token não emitido pelo servidor deve ser rejeitado"
    )

    lista = test(
        "GET /cardapio/lista — Lista de cardápios",
        "GET", "/cardapio/lista",
        token=TOKEN,
        checks=[
            ("Retorna uma lista não-vazia", lambda d: isinstance(d, list) and len(d) > 0),
            ("Cada item tem id e titulo",
             lambda d: all("id" in c and "titulo" in c for c in d)),
        ]
    )

    cardapio = test(
        "GET /cardapio/1 — Cardápio corrente",
        "GET", "/cardapio/1",
        token=TOKEN,
        checks=[
            ("Possui título",     lambda d: "titulo" in d and d["titulo"]),
            ("Possui lista de itens", lambda d: "itens" in d and len(d["itens"]) > 0),
        ]
    )

    if cardapio and "itens" in cardapio:
        print(f"{C.DIM}       ↳ {len(cardapio['itens'])} itens no cardápio{C.RESET}")
        for item in cardapio["itens"]:
            disp = "✓" if not item.get("sugestaoDoChef") else "⭐ Chef"
            print(f"{C.DIM}         • [{item.get('id')}] {item.get('descricao')} "
                  f"— R$ {item.get('preco', 0)/100:.2f} {disp}{C.RESET}")

    test(
        "GET /cardapio/999 — Cardápio inexistente",
        "GET", "/cardapio/999",
        expected_status=500,
        token=TOKEN,
        description="Espera erro ao buscar cardápio que não existe"
    )

    # ─────────────────────────────────────────────────────────
    section("5. PEDIDO — Submissão para aprovação (UC4)")
    # ─────────────────────────────────────────────────────────

    pedido_body = {
        "clienteCpf": "9001",
        "enderecoEntrega": "Rua dos Testes, 42",
        "itens": [
            {"produtoId": 1, "quantidade": 2},
            {"produtoId": 3, "quantidade": 1}
        ]
    }

    # Sanity check de auth para POST /pedidos
    test(
        "POST /pedidos — Sem token → 401",
        "POST", "/pedidos",
        body=pedido_body,
        expected_status=401,
        description="Submissão de pedido sem Bearer token deve retornar 401"
    )

    pedido = test(
        "POST /pedidos — Criar pedido (cliente 9001)",
        "POST", "/pedidos",
        body=pedido_body,
        token=TOKEN,
        checks=[
            ("Status é APROVADO",  lambda d: d.get("status") == "APROVADO"),
            ("Possui id",          lambda d: d.get("id") is not None),
            ("Valor > 0",          lambda d: d.get("valor", 0) > 0),
            ("Impostos > 0",       lambda d: d.get("impostos", 0) > 0),
            ("Valor cobrado > 0",  lambda d: d.get("valorCobrado", 0) > 0),
        ]
    )

    pedido_id = pedido.get("id") if pedido else None

    if pedido:
        print(f"{C.DIM}       ↳ Pedido #{pedido_id}")
        print(f"         Valor:    R$ {pedido['valor']/100:.2f}")
        print(f"         Impostos: R$ {pedido['impostos']/100:.2f}")
        print(f"         Desconto: R$ {pedido['desconto']/100:.2f}")
        print(f"         Cobrado:  R$ {pedido['valorCobrado']/100:.2f}{C.RESET}")

    # Pedido com dados inválidos (sem itens)
    test(
        "POST /pedidos — Pedido sem itens (erro validação)",
        "POST", "/pedidos",
        body={"clienteCpf": "9001", "enderecoEntrega": "Rua X", "itens": []},
        expected_status=400,
        token=TOKEN,
        description="Body com lista de itens validação vazia deve retornar 400"
    )

    # Pedido com cliente inexistente
    test(
        "POST /pedidos — Cliente inexistente",
        "POST", "/pedidos",
        body={
            "clienteCpf": "0000",
            "enderecoEntrega": "Rua Nenhuma",
            "itens": [{"produtoId": 1, "quantidade": 1}]
        },
        expected_status=400,
        token=TOKEN,
        description="CPF que não existe no banco deve retornar 400"
    )

    # Pedido com produto inexistente
    test(
        "POST /pedidos — Produto inexistente",
        "POST", "/pedidos",
        body={
            "clienteCpf": "9001",
            "enderecoEntrega": "Rua Nenhuma",
            "itens": [{"produtoId": 999, "quantidade": 1}]
        },
        expected_status=400,
        token=TOKEN,
        description="Produto que não existe deve retornar 400"
    )

    # ─────────────────────────────────────────────────────────
    section("6. STATUS DO PEDIDO (UC5)")
    # ─────────────────────────────────────────────────────────

    if pedido_id:
        status_resp = test(
            f"GET /pedidos/{pedido_id}/status — Status do pedido criado",
            "GET", f"/pedidos/{pedido_id}/status",
            token=TOKEN,
            checks=[
                ("Status é APROVADO",       lambda d: d.get("status") == "APROVADO"),
                ("Possui dataHoraCriacao",   lambda d: d.get("dataHoraCriacao") is not None),
                ("Possui enderecoEntrega",   lambda d: d.get("enderecoEntrega") is not None),
                ("Possui valorCobrado",      lambda d: d.get("valorCobrado") is not None),
            ]
        )

        if status_resp:
            print(f"{C.DIM}       ↳ Status: {status_resp['status']}")
            print(f"         Criado em: {status_resp['dataHoraCriacao']}")
            print(f"         Entrega: {status_resp['enderecoEntrega']}{C.RESET}")
    else:
        print(f"{C.YELLOW}  ⚠ Testes de UC5 pulados — pedido não foi criado{C.RESET}")

    test(
        "GET /pedidos/99999/status — Pedido inexistente",
        "GET", "/pedidos/99999/status",
        expected_status=400,
        token=TOKEN,
        description="Pedido inexistente deve retornar 400"
    )

    # ─────────────────────────────────────────────────────────
    section("7. CANCELAMENTO DE PEDIDO (UC6)")
    # ─────────────────────────────────────────────────────────

    if pedido_id:
        # Cancelar o pedido aprovado → deve funcionar
        cancel_resp = test(
            f"PUT /pedidos/{pedido_id}/cancelar — Cancelar pedido aprovado",
            "PUT", f"/pedidos/{pedido_id}/cancelar",
            token=TOKEN,
            checks=[
                ("Status é CANCELADO", lambda d: d.get("status") == "CANCELADO"),
                ("ID confere",         lambda d: d.get("id") == pedido_id),
            ]
        )

        if cancel_resp:
            print(f"{C.DIM}       ↳ Pedido #{cancel_resp['id']} → {cancel_resp['status']}{C.RESET}")

        # Verificar que o status mudou para CANCELADO
        test(
            f"GET /pedidos/{pedido_id}/status — Confirmar cancelamento",
            "GET", f"/pedidos/{pedido_id}/status",
            token=TOKEN,
            checks=[
                ("Status agora é CANCELADO", lambda d: d.get("status") == "CANCELADO"),
            ],
            description="Após cancelar, o status deve persistir como CANCELADO"
        )

        # Tentar cancelar novamente → deve falhar com 422
        test(
            f"PUT /pedidos/{pedido_id}/cancelar — Cancelar novamente (deve falhar)",
            "PUT", f"/pedidos/{pedido_id}/cancelar",
            expected_status=422,
            token=TOKEN,
            description="Pedido já cancelado não pode ser cancelado novamente"
        )
    else:
        print(f"{C.YELLOW}  ⚠ Testes de UC6 pulados — pedido não foi criado{C.RESET}")

    test(
        "PUT /pedidos/99999/cancelar — Cancelar pedido inexistente",
        "PUT", "/pedidos/99999/cancelar",
        expected_status=400,
        token=TOKEN,
        description="Pedido inexistente deve retornar 400"
    )

    # ─────────────────────────────────────────────────────────
    section("8. FLUXO COMPLETO — Novo pedido + consulta + cancela")
    # ─────────────────────────────────────────────────────────

    # Criar outro pedido com o segundo cliente
    pedido2_body = {
        "clienteCpf": "9002",
        "enderecoEntrega": "Av. Central, 200",
        "itens": [
            {"produtoId": 2, "quantidade": 1}
        ]
    }

    TOKEN_9002 = auth_9002["token"] if auth_9002 else None

    pedido2 = test(
        "POST /pedidos — Criar pedido (cliente 9002)",
        "POST", "/pedidos",
        body=pedido2_body,
        token=TOKEN_9002,
        checks=[
            ("Status é APROVADO", lambda d: d.get("status") == "APROVADO"),
        ]
    )

    pedido2_id = pedido2.get("id") if pedido2 else None

    if pedido2_id:
        # Consultar status
        test(
            f"GET /pedidos/{pedido2_id}/status — Status do 2o pedido",
            "GET", f"/pedidos/{pedido2_id}/status",
            token=TOKEN_9002,
            checks=[
                ("Status é APROVADO", lambda d: d.get("status") == "APROVADO"),
            ]
        )

        # Cancelar
        test(
            f"PUT /pedidos/{pedido2_id}/cancelar — Cancelar 2o pedido",
            "PUT", f"/pedidos/{pedido2_id}/cancelar",
            token=TOKEN_9002,
            checks=[
                ("Status é CANCELADO", lambda d: d.get("status") == "CANCELADO"),
            ]
        )

        # Confirmar status final
        test(
            f"GET /pedidos/{pedido2_id}/status — Status final do 2o pedido",
            "GET", f"/pedidos/{pedido2_id}/status",
            token=TOKEN_9002,
            checks=[
                ("Status é CANCELADO", lambda d: d.get("status") == "CANCELADO"),
            ]
        )

    # ─────────────────────────────────────────────────────────
    section("9. PAGAMENTO E SIMULAÇÃO DE ESTADOS (UC7)")
    # ─────────────────────────────────────────────────────────

    # Criar um novo pedido para testar o fluxo de pagamento completo
    pedido_pag_body = {
        "clienteCpf": "9001",
        "enderecoEntrega": "Rua do Pagamento, 99",
        "itens": [
            {"produtoId": 1, "quantidade": 1}
        ]
    }

    pedido_pag = test(
        "POST /pedidos — Criar pedido para UC7",
        "POST", "/pedidos",
        body=pedido_pag_body,
        token=TOKEN,
        checks=[
            ("Status é APROVADO", lambda d: d.get("status") == "APROVADO"),
        ]
    )

    pedido_pag_id = pedido_pag.get("id") if pedido_pag else None

    if pedido_pag_id:
        # Pagar pedido → deve retornar status PAGO
        test(
            f"PUT /pedidos/{pedido_pag_id}/pagar — Efetuar pagamento",
            "PUT", f"/pedidos/{pedido_pag_id}/pagar",
            expected_status=200,
            token=TOKEN,
            checks=[
                ("Status é PAGO", lambda d: d.get("status") == "PAGO"),
                ("ID confere", lambda d: d.get("id") == pedido_pag_id),
            ]
        )

        # Tentar pagar novamente → deve falhar com 422
        test(
            f"PUT /pedidos/{pedido_pag_id}/pagar — Pagar novamente (deve falhar)",
            "PUT", f"/pedidos/{pedido_pag_id}/pagar",
            expected_status=422,
            token=TOKEN,
            description="Pedido já pago não pode ser pago novamente"
        )

        # Tentar cancelar pedido pago → deve falhar com 422
        test(
            f"PUT /pedidos/{pedido_pag_id}/cancelar — Cancelar pedido pago (deve falhar)",
            "PUT", f"/pedidos/{pedido_pag_id}/cancelar",
            expected_status=422,
            token=TOKEN,
            description="Pedido pago não pode ser cancelado"
        )

        # Acompanhar a simulação assíncrona com polling resiliente
        # Esperado: 2s -> AGUARDANDO, 4s -> PREPARACAO, 6s -> PRONTO, 8s -> TRANSPORTE, 10s -> ENTREGUE
        estados_esperados = ["AGUARDANDO", "PREPARACAO", "PRONTO", "TRANSPORTE", "ENTREGUE"]
        auth_headers = {"Authorization": f"Bearer {TOKEN}"} if TOKEN else None

        print("       ↳ Monitorando transições de status assíncronas...")
        for i, estado in enumerate(estados_esperados):
            alcançado = False
            for _ in range(40):  # até 8 segundos por estado (40 * 0.2s)
                resp = requests.get(f"{BASE_URL}/pedidos/{pedido_pag_id}/status", headers=auth_headers)
                if resp.status_code == 200:
                    status_atual = resp.json().get("status")
                    if status_atual == estado or (status_atual in estados_esperados and estados_esperados.index(status_atual) > i):
                        alcançado = True
                        break
                time.sleep(0.2)

            test(
                f"GET /pedidos/{pedido_pag_id}/status — Verificando estado: {estado}",
                "GET", f"/pedidos/{pedido_pag_id}/status",
                token=TOKEN,
                checks=[
                    (f"Status atingiu/passou por {estado}",
                     lambda d, exp=estado, idx=i: d.get("status") == exp or (d.get("status") in estados_esperados and estados_esperados.index(d.get("status")) > idx)),
                ]
            )

        print(f"       ↳ Fluxo de simulação concluído com sucesso!")
    else:
        print(f"{C.YELLOW}  ⚠ Testes de UC7 pulados — pedido de teste não foi criado{C.RESET}")

    # ─────────────────────────────────────────────────────────
    section("10. UC8 — Listar pedidos entregues entre datas")
    # ─────────────────────────────────────────────────────────

    # data.sql semeia 3 pedidos ENTREGUE em 2026-05-20 / 21 / 22
    entregues_all = test(
        "GET /pedidos/entregues — Intervalo cobrindo todos os seeds",
        "GET", "/pedidos/entregues?inicio=2026-05-01T00:00:00&fim=2026-05-31T23:59:59",
        checks=[
            ("Retorna lista", lambda d: isinstance(d, list)),
            ("Inclui os 3 pedidos seed", lambda d: len(d) >= 3),
            ("Todos os itens têm id e cliente",
             lambda d: all(p.get("id") and p.get("clienteCpf") for p in d)),
        ]
    )

    if entregues_all:
        print(f"{C.DIM}       ↳ {len(entregues_all)} pedido(s) entregue(s) no intervalo{C.RESET}")

    test(
        "GET /pedidos/entregues — Intervalo vazio (datas no futuro)",
        "GET", "/pedidos/entregues?inicio=2099-01-01T00:00:00&fim=2099-12-31T23:59:59",
        checks=[
            ("Lista vazia", lambda d: isinstance(d, list) and len(d) == 0),
        ]
    )

    test(
        "GET /pedidos/entregues — Parâmetro de data inválido → 400",
        "GET", "/pedidos/entregues?inicio=data-ruim&fim=2026-12-31T23:59:59",
        expected_status=400,
        description="Data fora do formato ISO deve retornar 400"
    )

    # ─────────────────────────────────────────────────────────
    section("11. UC9 — Listar pedidos entregues de um cliente")
    # ─────────────────────────────────────────────────────────

    entregues_9001 = test(
        "GET /pedidos/entregues/9001 — Pedidos entregues do cliente 9001",
        "GET", "/pedidos/entregues/9001?inicio=2026-05-01T00:00:00&fim=2026-05-31T23:59:59",
        checks=[
            ("Retorna lista", lambda d: isinstance(d, list)),
            ("Cliente 9001 tem 2 pedidos seed entregues", lambda d: len(d) >= 2),
            ("Todos os pedidos têm itens",
             lambda d: all(p.get("itens") and len(p["itens"]) > 0 for p in d)),
        ]
    )

    if entregues_9001:
        print(f"{C.DIM}       ↳ {len(entregues_9001)} pedido(s) entregue(s) do cliente 9001{C.RESET}")

    test(
        "GET /pedidos/entregues/9002 — Pedidos entregues do cliente 9002",
        "GET", "/pedidos/entregues/9002?inicio=2026-05-01T00:00:00&fim=2026-05-31T23:59:59",
        checks=[
            ("Cliente 9002 tem 1 pedido seed entregue", lambda d: len(d) >= 1),
            ("Todos os pedidos têm itens",
             lambda d: all(p.get("itens") and len(p["itens"]) > 0 for p in d)),
        ]
    )

    test(
        "GET /pedidos/entregues/0000 — Cliente inexistente → 400",
        "GET", "/pedidos/entregues/0000?inicio=2026-05-01T00:00:00&fim=2026-05-31T23:59:59",
        expected_status=400,
        description="CPF não cadastrado deve retornar 400"
    )

    # ==========================================================
    #  RELATÓRIO FINAL
    # ==========================================================

    print(f"\n{C.BOLD}{C.CYAN}{'═' * 60}")
    print(f"  📊  RESULTADO FINAL")
    print(f"{'═' * 60}{C.RESET}\n")

    print(f"  Total:     {total}")
    print(f"  {C.GREEN}Passou:    {passed}{C.RESET}")

    if failed > 0:
        print(f"  {C.RED}Falhou:    {failed}{C.RESET}")
        print(f"\n{C.RED}{C.BOLD}  ── DETALHES DAS FALHAS ──{C.RESET}\n")
        for err in errors:
            print(f"{C.RED}{err}{C.RESET}\n")
    else:
        print(f"  Falhou:    0")

    pct = (passed / total * 100) if total > 0 else 0

    if pct == 100:
        print(f"\n  {C.GREEN}{C.BOLD}🎉 TODOS OS TESTES PASSARAM! ({passed}/{total}){C.RESET}\n")
    elif pct >= 80:
        print(f"\n  {C.YELLOW}{C.BOLD}⚠ {pct:.0f}% dos testes passaram ({passed}/{total}){C.RESET}\n")
    else:
        print(f"\n  {C.RED}{C.BOLD}❌ {pct:.0f}% dos testes passaram ({passed}/{total}){C.RESET}\n")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
