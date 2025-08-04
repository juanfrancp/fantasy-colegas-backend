package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // Relación con League: Un usuario puede ser administrador de muchas ligas
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<League> leaguesAsAdmin;

    // Relación con League: Un usuario puede pertenecer a muchas ligas
    @ManyToMany(mappedBy = "participants")
    private Set<League> leaguesAsParticipant;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // Solo compara por el ID
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        // Solo usa el ID para el hash
        return getClass().hashCode();
    }
}