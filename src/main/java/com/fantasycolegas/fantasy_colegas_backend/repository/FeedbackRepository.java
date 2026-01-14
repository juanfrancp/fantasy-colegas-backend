package com.fantasycolegas.fantasy_colegas_backend.repository;
import com.fantasycolegas.fantasy_colegas_backend.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByIsResolvedFalseOrderBySentAtDesc(); // Útil para el admin
}