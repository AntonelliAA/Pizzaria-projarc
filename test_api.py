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
         timeout: int = 10):
    """
    Executa um teste HTTP e valida o resultado.

    checks: lista de tuplas (descricao, funcao_lambda_sobre_json)
            ex: [("status é APROVADO", lambda j: j["status"] == "APROVADO")]
    """
    global total, passed, failed

    total += 1
    url = f"{BASE_URL}{path}"
    prefix = f"  [{total:02d}] {name}"

    try:
        if method == "GET":
            resp = requests.get(url, timeout=timeout)
        elif method == "POST":
            resp = requests.post(url, json=body, timeout=timeout)
        elif method == "PUT":
            resp = requests.put(url, json=body, timeout=timeout)
        elif method == "DELETE":
            resp = requests.delete(url, timeout=timeout)
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

    # ─────────────────────────────────────────────────────────
    section("4. CARDÁPIO — Listar e recuperar (UC3)")
    # ─────────────────────────────────────────────────────────

    lista = test(
        "GET /cardapio/lista — Lista de cardápios",
        "GET", "/cardapio/lista",
        checks=[
            ("Retorna uma lista não-vazia", lambda d: isinstance(d, list) and len(d) > 0),
            ("Cada item tem id e titulo",
             lambda d: all("id" in c and "titulo" in c for c in d)),
        ]
    )

    cardapio = test(
        "GET /cardapio/1 — Cardápio corrente",
        "GET", "/cardapio/1",
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

    pedido = test(
        "POST /pedidos — Criar pedido (cliente 9001)",
        "POST", "/pedidos",
        body=pedido_body,
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
        description="Produto que não existe deve retornar 400"
    )

    # ─────────────────────────────────────────────────────────
    section("6. STATUS DO PEDIDO (UC5)")
    # ─────────────────────────────────────────────────────────

    if pedido_id:
        status_resp = test(
            f"GET /pedidos/{pedido_id}/status — Status do pedido criado",
            "GET", f"/pedidos/{pedido_id}/status",
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
            description="Pedido já cancelado não pode ser cancelado novamente"
        )
    else:
        print(f"{C.YELLOW}  ⚠ Testes de UC6 pulados — pedido não foi criado{C.RESET}")

    test(
        "PUT /pedidos/99999/cancelar — Cancelar pedido inexistente",
        "PUT", "/pedidos/99999/cancelar",
        expected_status=400,
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

    pedido2 = test(
        "POST /pedidos — Criar pedido (cliente 9002)",
        "POST", "/pedidos",
        body=pedido2_body,
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
            checks=[
                ("Status é APROVADO", lambda d: d.get("status") == "APROVADO"),
            ]
        )

        # Cancelar
        test(
            f"PUT /pedidos/{pedido2_id}/cancelar — Cancelar 2o pedido",
            "PUT", f"/pedidos/{pedido2_id}/cancelar",
            checks=[
                ("Status é CANCELADO", lambda d: d.get("status") == "CANCELADO"),
            ]
        )

        # Confirmar status final
        test(
            f"GET /pedidos/{pedido2_id}/status — Status final do 2o pedido",
            "GET", f"/pedidos/{pedido2_id}/status",
            checks=[
                ("Status é CANCELADO", lambda d: d.get("status") == "CANCELADO"),
            ]
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
