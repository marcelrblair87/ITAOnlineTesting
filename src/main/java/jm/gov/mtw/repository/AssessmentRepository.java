package jm.gov.mtw.repository;

import jm.gov.mtw.models.Assessment;
import jm.gov.mtw.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Optional<Assessment> findByUserAndOriginalAttemptDate(User user, LocalDate date);
}
