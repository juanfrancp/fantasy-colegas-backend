package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.FeedbackCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.Feedback;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.FeedbackRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public void createFeedback(FeedbackCreateDto dto) {
        // Obtenemos el usuario autenticado automáticamente
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Feedback feedback = new Feedback(user, dto.getType(), dto.getMessage());
        feedbackRepository.save(feedback);
    }
}