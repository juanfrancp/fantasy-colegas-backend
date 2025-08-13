package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link User}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de los usuarios, incluyendo búsquedas por nombre de usuario y correo electrónico.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username El nombre de usuario.
     * @return Un objeto {@link Optional} que puede contener el usuario encontrado.
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email La dirección de correo electrónico.
     * @return Un objeto {@link Optional} que puede contener el usuario encontrado.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su nombre de usuario o su dirección de correo electrónico.
     *
     * @param username El nombre de usuario.
     * @param email    La dirección de correo electrónico.
     * @return Un objeto {@link Optional} que puede contener el usuario encontrado.
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
}