package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestResponseDto {

    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime requestDate;

}