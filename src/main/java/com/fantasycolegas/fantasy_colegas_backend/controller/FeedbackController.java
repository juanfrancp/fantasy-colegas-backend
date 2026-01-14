package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.FeedbackCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<Void> sendFeedback(@RequestBody FeedbackCreateDto dto) {
        feedbackService.createFeedback(dto);
        return ResponseEntity.ok().build();
    }

    // Aquí podrías añadir un @GetMapping para que el ADMIN vea los mensajes
}