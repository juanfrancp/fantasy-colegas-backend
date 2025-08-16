package com.fantasycolegas.fantasy_colegas_backend.config;

import com.fantasycolegas.fantasy_colegas_backend.util.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Clase de configuración de seguridad para la aplicación.
 * <p>
 * Esta clase configura la cadena de filtros de seguridad, el codificador de contraseñas
 * y las reglas de autorización para las peticiones HTTP.
 * Utiliza JSON Web Tokens (JWT) para la autenticación y establece una política de sesión sin estado.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * Configura la cadena de filtros de seguridad para las peticiones HTTP.
     * <p>
     * Define las reglas de autorización para diferentes endpoints (permitiendo acceso a la consola H2
     * y a los endpoints de autenticación sin necesidad de autenticación), y añade el filtro JWT
     * para la autenticación de tokens antes del filtro de autenticación por nombre de usuario y contraseña.
     * </p>
     *
     * @param http El objeto HttpSecurity para configurar la seguridad web.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(authorize -> authorize.requestMatchers("/h2-console/**", "/api/auth/**").permitAll().requestMatchers("/api/users/**", "/api/leagues/**").authenticated().anyRequest().authenticated()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())).exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Proporciona un bean para la clase AuthenticationManager.
     *
     * @param authenticationConfiguration La configuración de autenticación.
     * @return El gestor de autenticación.
     * @throws Exception Si ocurre un error al obtener el gestor de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Proporciona un bean para un codificador de contraseñas.
     * <p>
     * Utiliza BCryptPasswordEncoder para codificar y verificar las contraseñas.
     * </p>
     *
     * @return Una instancia de {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}