package com.fantasycolegas.fantasy_colegas_backend.util;

import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Filtro de seguridad para la autenticación basada en JWT.
 * <p>
 * Este filtro se ejecuta una vez por cada petición HTTP y se encarga de
 * interceptar el encabezado 'Authorization' para extraer, validar y
 * procesar el token JWT. Si el token es válido, establece la autenticación
 * en el contexto de seguridad de Spring.
 * </p>
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Lógica principal del filtro que se ejecuta para cada solicitud.
     * <p>
     * 1. Extrae el token JWT del encabezado 'Authorization'.
     * 2. Si el token es válido y no hay una autenticación previa en el contexto,
     * obtiene los detalles del usuario.
     * 3. Valida el token y crea un objeto de autenticación.
     * 4. Establece la autenticación en el {@link SecurityContextHolder}.
     * 5. Permite que la cadena de filtros continúe.
     * </p>
     *
     * @param request  La solicitud HTTP.
     * @param response La respuesta HTTP.
     * @param chain    La cadena de filtros.
     * @throws ServletException Si ocurre un error de servlet.
     * @throws IOException      Si ocurre un error de E/S.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}