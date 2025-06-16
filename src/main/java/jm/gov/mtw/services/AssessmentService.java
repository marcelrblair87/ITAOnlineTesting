package jm.gov.mtw.services;

import jm.gov.mtw.dto.AnswerDto;
import jm.gov.mtw.dto.AssessmentResultDto;
import jm.gov.mtw.models.*;
import jm.gov.mtw.repository.AppointmentRepository;
import jm.gov.mtw.repository.AssessmentRepository;
import jm.gov.mtw.repository.QuestionRepository;
import jm.gov.mtw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssessmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private UserRepository userRepository;

    public Assessment startAssessment(String trn) {
        LocalDateTime today = LocalDateTime.now();
        System.out.println("Trn: " + trn);
        Appointment appointment = appointmentRepository.findByUser_Trn(trn).stream().findFirst().orElseThrow();
                /*appointmentRepository.findByTrnAndAppointmentDate(trn, today)
                .orElseThrow(() -> new RuntimeException("No appointment found."));*/

        if (!appointment.getAppointmentDateTime().toLocalDate().isEqual(today.toLocalDate())) {
            throw new RuntimeException("Assessment only allowed on appointment day.");
        }

        User user = userRepository.findByTrn(trn)
                .orElseThrow(() -> new RuntimeException("No user found."));

        // Prevent multiple sessions
        if (assessmentRepository.findByUserAndOriginalAttemptDate(user, today.toLocalDate()).isPresent()) {
            throw new RuntimeException("Assessment has already started for today.");
        }

        List<Question> randomQuestions = questionRepository.findAll()
                .stream()
                .sorted((a, b) -> new Random().nextInt(3) - 1) // shuffle
                .limit(20)
                .collect(Collectors.toCollection(ArrayList::new));

        Assessment session = new Assessment();
        session.setUser(user);
        session.setOriginalAttemptDate(today.toLocalDate());
        session.setAssessmentStartTime(LocalDateTime.now());
        session.setQuestions(new ArrayList<>(randomQuestions));
        session.setCompleted(false);

        System.out.println("Questions class: " + session.getQuestions().getClass());
        return assessmentRepository.save(session);
    }

    public AssessmentResultDto submitAssessment(String trn, List<AnswerDto> answers) {
        LocalDateTime today = LocalDateTime.now();

        Appointment appointment = appointmentRepository.findByUser_Trn(trn).stream()
                .filter(app -> app.getAppointmentDateTime().toLocalDate().isEqual(today.toLocalDate()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No appointment found."));


        User user = userRepository.findByTrn(trn)
                .orElseThrow(() -> new RuntimeException("No user found."));

        Assessment assessment = assessmentRepository.findByUserAndOriginalAttemptDate(user, today.toLocalDate())
                .orElseThrow(() -> new RuntimeException("No assessment found."));

        if (Duration.between(assessment.getAssessmentStartTime(), LocalDateTime.now()).toMinutes() > 20) {
            throw new IllegalStateException("Assessment time exceeded.");
        }

        int score = calculateScore( answers);
        assessment.setScore(score);

        if (score >= 15) {
            assessment.setStatus(Assessment.AssessmentStatus.PASS);
            assessment.setReferenceNumber(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else {
            assessment.setStatus(Assessment.AssessmentStatus.FAIL);
            LocalDateTime rescheduleDate = appointment.getAppointmentDateTime().plusDays(10);
            appointment.setAppointmentDateTime(rescheduleDate);
            appointmentRepository.save(appointment);
        }


        System.out.println("Questions class: " + assessment.getQuestions().getClass());
        assessment.setCompleted(true);
        assessmentRepository.save(assessment);

        return AssessmentResultDto.from(assessment);
    }

    public boolean isAssessmentActive(Long sessionId) {
        Assessment session = assessmentRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return !session.isCompleted()
                && Duration.between(session.getAssessmentStartTime(), LocalDateTime.now()).toMinutes() <= 20;
    }

    private int calculateScore(List<AnswerDto> answers) {
        int score = 0;
        for (AnswerDto answer : answers) {
            Question question = questionRepository.findById(answer.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            if (question.getCorrectAnswer().equalsIgnoreCase(answer.getSelectedAnswer())) {
                score++;
            }
        }
        return score;
    }

    public void completeAssessment(Long sessionId) {
        Assessment session = assessmentRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setCompleted(true);
        assessmentRepository.save(session);
    }
}
