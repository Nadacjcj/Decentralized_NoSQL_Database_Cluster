package org.example.node.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long expiration = 1000 * 60 * 60 * 5; // 5 hours

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String id, String username) {
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("id", id, "username", username))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public String extractUsername(String token) {
        return parseToken(token).getBody().get("username", String.class);
    }

    public String extractId(String token) {
        return parseToken(token).getBody().get("id", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    public Map<String, Object> validateAndExtract(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Map.of(
                    "id", claims.get("id", String.class),
                    "username", claims.getSubject()
            );
        } catch (JwtException e) {
            return null;
        }
    }

}
