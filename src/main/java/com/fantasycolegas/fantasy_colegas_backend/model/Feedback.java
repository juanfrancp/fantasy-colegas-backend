package com.fantasycolegas.fantasy_colegas_backend.model;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.FeedbackType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private FeedbackType type;

    @Column(length = 1000)
    private String message;

    private LocalDateTime sentAt;

    private boolean isResolved = false;

    public Feedback(User user, FeedbackType type, String message) {
        this.user = user;
        this.type = type;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }
}