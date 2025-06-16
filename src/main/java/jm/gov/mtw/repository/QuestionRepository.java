package jm.gov.mtw.repository;

import jm.gov.mtw.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuestionRepository extends JpaRepository<Question, Long> {}


