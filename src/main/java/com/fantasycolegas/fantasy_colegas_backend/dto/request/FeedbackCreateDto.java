package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.FeedbackType;
import lombok.Data;

@Data
public class FeedbackCreateDto {
    private FeedbackType type;
    private String message;
}