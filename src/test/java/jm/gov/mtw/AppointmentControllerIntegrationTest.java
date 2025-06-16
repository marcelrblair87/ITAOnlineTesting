package jm.gov.mtw;

import jm.gov.mtw.models.Appointment;
import jm.gov.mtw.models.User;
import jm.gov.mtw.repository.AppointmentRepository;
import jm.gov.mtw.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll(); // Clean the DB before each test
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateValidAppointment() throws Exception {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setTrn("123456789");
        userRepository.save(user);

        String appointmentJson = """
            {
                "trn": "123456789",
                "appointmentDate": "2030-12-31T10:00"
            }
        """;

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.firstName").value("Jane"))
                .andExpect(jsonPath("$.user.trn").value("123456789"));
    }

    @Test
    void shouldRejectPastAppointmentDate() throws Exception {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setTrn("123456789");
        userRepository.save(user);

        String pastAppointment = """
            {
                "trn": "123456789",
                "appointmentDate": "2000-01-01T10:00"
            }
        """;

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pastAppointment))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnAppointmentsByTRN() throws Exception {
        User user = new User();
        user.setFirstName("Alice");
        user.setLastName("Brown");
        user.setTrn("987654321");
        userRepository.save(user);

        Appointment appt = new Appointment();
        appt.setUser(user);
        appt.setAppointmentDateTime(LocalDateTime.of(2031, 1, 10, 14, 0));
        appointmentRepository.save(appt);

        mockMvc.perform(get("/api/appointments/search")
                        .param("trn", "987654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user.firstName").value("Alice"))
                .andExpect(jsonPath("$[0].user.trn").value("987654321"));
    }

    @Test
    void shouldReturnAppointmentsByDate() throws Exception {
        User user1 = new User();
        user1.setFirstName("Tom");
        user1.setLastName("Green");
        user1.setTrn("111222333");
        userRepository.save(user1);

        User user2 = new User();
        user2.setFirstName("Tina");
        user2.setLastName("White");
        user2.setTrn("444555666");
        userRepository.save(user2);

        Appointment appt1 = new Appointment();
        appt1.setUser(user1);
        appt1.setAppointmentDateTime(LocalDateTime.of(2032, 5, 1, 9, 0));

        Appointment appt2 = new Appointment();
        appt2.setUser(user2);
        appt2.setAppointmentDateTime(LocalDateTime.of(2032, 5, 1, 11, 0));

        appointmentRepository.save(appt1);
        appointmentRepository.save(appt2);

        mockMvc.perform(get("/api/appointments/filter")
                        .param("date", "2032-05-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}