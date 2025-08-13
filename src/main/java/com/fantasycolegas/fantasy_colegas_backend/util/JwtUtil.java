package com.fantasycolegas.fantasy_colegas_backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Clase de utilidad para la generación y validación de JSON Web Tokens (JWT).
 * <p>
 * Gestiona la creación, extracción y validación de tokens JWT, utilizando
 * una clave secreta y un tiempo de expiración configurables.
 * </p>
 */
@Component
public class JwtUtil {

    /**
     * Clave secreta para firmar y verificar los tokens JWT.
     * Se inyecta desde la configuración de la aplicación.
     */
    @Value("${jwt.secret:defaultSecretForDevelopment}")
    private String secret;

    /**
     * Tiempo de expiración del token en milisegundos. Por defecto, una hora.
     * Se inyecta desde la configuración de la aplicación.
     */
    @Value("${jwt.expiration:3600000}") // 1 hora
    private long expiration;

    /**
     * Obtiene la clave de firma a partir del secreto configurado.
     *
     * @return Una clave secreta de tipo {@link SecretKey}.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el nombre de usuario (subject) de un token JWT.
     *
     * @param token El token JWT.
     * @return El nombre de usuario.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración de un token JWT.
     *
     * @param token El token JWT.
     * @return La fecha de expiración.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Método genérico para extraer un "claim" específico del token.
     *
     * @param token          El token JWT.
     * @param claimsResolver Función para resolver el claim a partir de los {@link Claims}.
     * @param <T>            El tipo de dato del claim.
     * @return El valor del claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims (cuerpo) del token.
     *
     * @param token El token JWT.
     * @return Todos los claims del token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Verifica si un token ha expirado.
     *
     * @param token El token JWT.
     * @return {@code true} si el token ha expirado, {@code false} en caso contrario.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Genera un token JWT para un usuario.
     *
     * @param userDetails Los detalles del usuario.
     * @return El token JWT generado.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Crea un token JWT a partir de los claims y el sujeto.
     *
     * @param claims  Los claims a incluir en el token.
     * @param subject El sujeto (nombre de usuario) del token.
     * @return El token JWT como cadena.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + expiration)).signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    /**
     * Valida un token JWT comparando el nombre de usuario y verificando si ha expirado.
     *
     * @param token       El token JWT.
     * @param userDetails Los detalles del usuario para la validación.
     * @return {@code true} si el token es válido, {@code false} en caso contrario.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}