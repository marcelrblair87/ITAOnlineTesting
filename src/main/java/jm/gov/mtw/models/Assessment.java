package jm.gov.mtw.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to user via TRN
    @ManyToOne
    @JoinColumn(name = "user_trn", referencedColumnName = "trn")
    private User user;

    private LocalDateTime assessmentStartTime;
    private boolean isCompleted;

    private Integer score;

    @Enumerated(EnumType.STRING)
    private AssessmentStatus status; // PASS / FAIL

    private String referenceNumber;

    @ManyToMany
    @JoinTable(
            name = "assessment_questions",
            joinColumns = @JoinColumn(name = "assessment_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>();

    // Original attempt time (used for rescheduling after failure)
    private LocalDate originalAttemptDate;

    public enum AssessmentStatus {
        PASS,
        FAIL
    }

    // Convenience methods
    public boolean isWithinAllowedTime() {
        return assessmentStartTime != null &&
                Duration.between(assessmentStartTime, LocalDateTime.now()).toMinutes() <= 20;
    }
}