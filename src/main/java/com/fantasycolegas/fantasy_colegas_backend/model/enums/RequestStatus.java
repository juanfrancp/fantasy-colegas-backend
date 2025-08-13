package com.fantasycolegas.fantasy_colegas_backend.model.enums;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 *
 * Enumeración que define los posibles estados de una solicitud.
 * <p>
 * Los estados posibles son {@code PENDING} (pendiente), {@code ACCEPTED} (aceptada)
 * y {@code REJECTED} (rechazada).
 * </p>
 */
public enum RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}