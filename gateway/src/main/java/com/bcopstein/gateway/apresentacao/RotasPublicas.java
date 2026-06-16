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
