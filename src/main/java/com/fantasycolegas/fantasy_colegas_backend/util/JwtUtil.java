package com.fantasycolegas.fantasy_colegas_backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Juan Francisco Carceles
 * @version 1.1
 * @since 01/08/2025
 * <p>
 * Clase de utilidad para la generación y validación de JSON Web Tokens (JWT).
 * <p>
 * Gestiona la creación, extracción y validación de tokens JWT, utilizando
 * una clave secreta y un tiempo de expiración configurables.
 * Actualizado a JJWT 0.12.x.
 * </p>
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretForDevelopment}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // 1 hora
    private long expiration;

    /**
     * Obtiene la clave de firma a partir del secreto configurado.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims (cuerpo) del token.
     * Actualizado para JJWT 0.12.x
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Antes: setSigningKey
                .build()
                .parseSignedClaims(token)    // Antes: parseClaimsJws
                .getPayload();               // Antes: getBody
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Crea un token JWT.
     * Actualizado para JJWT 0.12.x
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)           // Antes: setClaims
                .subject(subject)         // Antes: setSubject
                .issuedAt(new Date(System.currentTimeMillis())) // Antes: setIssuedAt
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Antes: setExpiration
                .signWith(getSigningKey(), Jwts.SIG.HS256) // Antes: SignatureAlgorithm.HS256
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}