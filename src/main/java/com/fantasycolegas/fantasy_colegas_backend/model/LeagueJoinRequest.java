package com.fantasycolegas.fantasy_colegas_backend.model;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Entidad JPA que representa una solicitud de unión a una liga.
 * <p>
 * Almacena la información de una petición de un usuario para unirse a una liga privada,
 * incluyendo el usuario que la envía, la liga a la que se desea unir, la fecha de la solicitud
 * y el estado actual de la misma.
 * </p>
 */
@Entity
@Table(name = "league_join_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;
}