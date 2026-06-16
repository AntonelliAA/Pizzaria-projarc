package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IAuthTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que protege os endpoints marcados como 🔒 no enunciado (UC3–UC7).
 *
 * Espera o header "Authorization: Bearer <token>" com um token válido
 * emitido pelo {@link IAuthTokenService} no login (UC2).
 *
 * Endpoints abertos (não passam pelo check): UC1 (POST /clientes),
 * UC2 (POST /auth), UC8/UC9 (GET /pedidos/entregues*), raiz, swagger, h2.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final Pattern PEDIDO_PROTEGIDO =
            Pattern.compile("^/pedidos/\\d+/(status|cancelar|pagar)$");

    private final IAuthTokenService tokens;

    public AuthFilter(IAuthTokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse resp,
                                    FilterChain chain) throws ServletException, IOException {

        if (!isProtegido(req)) {
            chain.doFilter(req, resp);
            return;
        }

        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            unauthorized(resp, "Token ausente. Envie o header 'Authorization: Bearer <token>'.");
            return;
        }

        String token = header.substring("Bearer ".length()).trim();
        if (!tokens.validate(token)) {
            unauthorized(resp, "Token inválido ou expirado.");
            return;
        }

        req.setAttribute("cpfAutenticado", tokens.cpfFor(token));
        chain.doFilter(req, resp);
    }

    private boolean isProtegido(HttpServletRequest req) {
        String path = req.getRequestURI();
        String method = req.getMethod();

        // UC3 — carregar cardápio
        if (path.startsWith("/cardapio")) return true;

        // UC4 — submeter pedido (POST /pedidos exato)
        if ("POST".equals(method) && "/pedidos".equals(path)) return true;

        // UC5/UC6/UC7 — operações sobre um pedido específico
        if (PEDIDO_PROTEGIDO.matcher(path).matches()) return true;

        return false;
    }

    private void unauthorized(HttpServletResponse resp, String msg) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"erro\":\"" + msg + "\"}");
    }
}
