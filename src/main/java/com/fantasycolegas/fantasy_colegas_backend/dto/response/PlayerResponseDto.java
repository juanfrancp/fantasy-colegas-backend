package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la respuesta de los datos de un jugador.
 * <p>
 * Este objeto se utiliza para transferir una representación de un jugador,
 * incluyendo su información básica y sus puntos totales acumulados.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResponseDto {
    private Long id;
    private String name;
    private String image;
    private Integer totalPoints;

    private Double totalFieldPoints;
    private Double totalGoalkeeperPoints;

    // --- NUEVOS CAMPOS PARA EDICIÓN ---
    // Usamos objetos (Integer/Boolean) para permitir nulos si no hay stats
    private Integer golesMarcados;
    private Integer asistencias;
    private Integer fallosClarosDeGol;
    private Integer faltasCometidas;
    private Integer faltasRecibidas;
    private Integer penaltisCometidos;
    private Integer penaltisRecibidos;
    private Integer tarjetasAmarillas;
    private Integer tarjetasRojas;
    private Integer salvadasDeGol;

    // Portero
    private Integer paradasComoPortero;
    private Integer golesEncajadosComoPortero;
    private Integer penaltisParados;
    private Boolean porteriaImbatida;
}