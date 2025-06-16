package jm.gov.mtw;

import jakarta.transaction.Transactional;
import jm.gov.mtw.dto.AnswerDto;
import jm.gov.mtw.dto.AssessmentResultDto;
import jm.gov.mtw.models.Appointment;
import jm.gov.mtw.models.Assessment;
import jm.gov.mtw.models.Question;
import jm.gov.mtw.models.User;
import jm.gov.mtw.repository.AppointmentRepository;
import jm.gov.mtw.repository.AssessmentRepository;
import jm.gov.mtw.repository.QuestionRepository;
import jm.gov.mtw.repository.UserRepository;
import jm.gov.mtw.services.AssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class AssessmentServiceTest {

    @Autowired
    private AssessmentService assessmentService;
    @Autowired private UserRepository userRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private AssessmentRepository assessmentRepository;
    @Autowired private QuestionRepository questionRepository;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setTrn("123456789");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Appointment appt = new Appointment();
        appt.setUser(user);
        appt.setAppointmentDateTime(LocalDateTime.now());
        appt.setLocation("Downtown Hub");
        appointmentRepository.save(appt);

        for (int i = 1; i <= 25; i++) {
            Question q = new Question();
            q.setText("Q" + i + "?");
            q.setOptions(List.of("A", "B", "C", "D"));
            q.setCorrectAnswer("A");
            questionRepository.save(q);
        }
    }

    @Test
    void testStartAssessmentCreatesNewSession() {
        Assessment session = assessmentService.startAssessment("123456789");

        assertNotNull(session.getId());
        assertEquals("123456789", session.getUser().getTrn());
        assertEquals(20, session.getQuestions().size());
    }

    @Test
    void testSubmitAssessmentPasses() {
        Assessment started = assessmentService.startAssessment("123456789");

        List<AnswerDto> answers = started.getQuestions().stream()
                .map(qid -> new AnswerDto(qid.getId(), "A"))
                .collect(Collectors.toCollection(ArrayList::new));

        AssessmentResultDto result = assessmentService.submitAssessment("123456789", answers);

        assertEquals("PASS", result.getStatus());
        assertNotNull(result.getReference());
    }

    @Test
    void testSubmitAssessmentFailsAndReschedules() {
        Assessment started = assessmentService.startAssessment("123456789");

        List<AnswerDto> answers = started.getQuestions().stream()
                .map(q -> new AnswerDto(q.getId(), "Z")) // all incorrect
                .collect(Collectors.toList());

        AssessmentResultDto result = assessmentService.submitAssessment("123456789", answers);

        assertEquals("FAIL", result.getStatus());
        Appointment rescheduled = appointmentRepository.findByUser_Trn("123456789").get(0);
        assertEquals(LocalDate.now().plusDays(10), rescheduled.getAppointmentDateTime().toLocalDate());
    }

    @Test
    void testReferenceNumberGeneratedOnPass() {
        Assessment started = assessmentService.startAssessment("123456789");

        List<AnswerDto> answers = started.getQuestions().stream()
                .map(q -> new AnswerDto(q.getId(), "A")) // all correct
                .collect(Collectors.toList());

        AssessmentResultDto result = assessmentService.submitAssessment("123456789", answers);

        assertEquals("PASS", result.getStatus());
        assertNotNull(result.getReference());
        assertFalse(result.getReference().isBlank());
    }

    @Test
    void testAssessmentMarkedCompletedAfterSubmission() {
        Assessment started = assessmentService.startAssessment("123456789");

        List<AnswerDto> answers = started.getQuestions().stream()
                .map(q -> new AnswerDto(q.getId(), "A"))
                .collect(Collectors.toList());

        assessmentService.submitAssessment("123456789", answers);

        Assessment saved = assessmentRepository.findById(started.getId()).orElseThrow();
        assertTrue(saved.isCompleted());
    }
}
