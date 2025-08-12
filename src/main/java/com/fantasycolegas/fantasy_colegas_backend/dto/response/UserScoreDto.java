package com.fantasycolegas.fantasy_colegas_backend.dto.response;

public class UserScoreDto {

    private Long userId;
    private double totalPoints;

    // Constructor que inicializa los campos
    public UserScoreDto(Long userId, double totalPoints) {
        this.userId = userId;
        this.totalPoints = totalPoints;
    }

    // Getters para acceder a los datos
    public Long getUserId() {
        return userId;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    // Setters (opcionales) para modificar los datos, si es necesario
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTotalPoints(double totalPoints) {
        this.totalPoints = totalPoints;
    }
}