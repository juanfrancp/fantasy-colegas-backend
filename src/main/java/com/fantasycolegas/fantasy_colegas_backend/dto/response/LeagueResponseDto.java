package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResponseDto {
    private Long id;
    private String name;
    private String description;
    private String image;
    private boolean isPrivate;
    private String joinCode;
    private int numberOfPlayers;
    private UserResponseDto admin;
    private List<UserResponseDto> participants;
}