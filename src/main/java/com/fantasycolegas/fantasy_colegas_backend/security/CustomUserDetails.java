package com.fantasycolegas.fantasy_colegas_backend.security;

import com.fantasycolegas.fantasy_colegas_backend.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Clase que implementa {@link UserDetails} de Spring Security para almacenar los
 * detalles del usuario autenticado.
 * <p>
 * Contiene información básica del usuario como ID, nombre de usuario, contraseña y
 * los roles/autoridades.
 * </p>
 */
public class CustomUserDetails implements UserDetails {

    @Getter
    private Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor para crear una instancia de {@link CustomUserDetails}.
     *
     * @param id          El ID del usuario.
     * @param username    El nombre de usuario.
     * @param password    La contraseña del usuario.
     * @param authorities La colección de autoridades/roles del usuario.
     */
    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Método estático para construir una instancia de {@link CustomUserDetails} a partir de un objeto {@link User}.
     * <p>
     * Este método facilita la conversión de la entidad de usuario a la clase de detalles de usuario
     * que Spring Security requiere.
     * </p>
     *
     * @param user El objeto {@link User} a partir del cual se construirán los detalles.
     * @return Una nueva instancia de {@link CustomUserDetails}.
     */
    public static CustomUserDetails build(User user) {
        List<GrantedAuthority> authorities = user.getLeagueRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole()))
                .collect(Collectors.toList());

        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}