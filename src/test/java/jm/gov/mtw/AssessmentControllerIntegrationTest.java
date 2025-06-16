package jm.gov.mtw;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jm.gov.mtw.models.Appointment;
import jm.gov.mtw.models.Question;
import jm.gov.mtw.models.User;
import jm.gov.mtw.repository.AppointmentRepository;
import jm.gov.mtw.repository.QuestionRepository;
import jm.gov.mtw.repository.UserRepository;
import jm.gov.mtw.util.JwtUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssessmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserRepository userRepository;
    private String validJwt;

    @BeforeAll
    void setUp() {
        // Save a user
        User user = new User();
        user.setTrn("111222333");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userRepository.save(user);

        // Save an appointment scheduled for today
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setAppointmentDateTime(LocalDateTime.now()); // today
        appointmentRepository.save(appointment);

        // Insert 20 dummy questions
        for (int i = 1; i <= 20; i++) {
            Question question = new Question();
            question.setText("Sample Question " + i + "?");
            question.setOptions(Arrays.asList("A", "B", "C", "D"));
            question.setCorrectAnswer("A");
            questionRepository.save(question);
        }

        // Generate a valid JWT for that user
        validJwt = jwtUtil.generateToken("111222333",10 * 60 * 1000); // assume this method exists
    }


    @Test
    void testStartAssessmentShouldReturn20Questions() throws Exception {
        mockMvc.perform(post("/api/assessment/start?trn=111222333")
                        .cookie(new Cookie("accessToken", validJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions", hasSize(20)));
    }
}
