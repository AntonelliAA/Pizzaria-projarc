package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {
    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public String createToken(String cpf) {
        String t = UUID.randomUUID().toString();
        tokens.put(t, new TokenInfo(cpf, Instant.now().plus(Duration.ofHours(8))));
        return t;
    }

    public boolean validate(String token) {
        TokenInfo info = tokens.get(token);
        if (info == null) return false;
        if (Instant.now().isAfter(info.expiresAt)) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public String cpfFor(String token) {
        TokenInfo info = tokens.get(token);
        return info == null ? null : info.cpf;
    }

    private static class TokenInfo {
        final String cpf;
        final Instant expiresAt;
        TokenInfo(String cpf, Instant expiresAt) { this.cpf = cpf; this.expiresAt = expiresAt; }
    }
}
