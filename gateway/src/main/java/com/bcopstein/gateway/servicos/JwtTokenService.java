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
