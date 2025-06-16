package jm.gov.mtw;

import jakarta.validation.ValidationException;
import jm.gov.mtw.dto.AppointmentRequest;
import jm.gov.mtw.models.Appointment;
import jm.gov.mtw.models.User;
import jm.gov.mtw.repository.AppointmentRepository;
import jm.gov.mtw.repository.UserRepository;
import jm.gov.mtw.services.AppointmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class AppointmentServiceTest {

    @Autowired
    private AppointmentService service;

    @Autowired
    private AppointmentRepository repository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void testSuccessfulAppointmentCreation() {
        User user = new User();
        user.setTrn("123456789");
        user.setFirstName("John");
        user.setLastName("Doe");
        userRepository.save(user);

        AppointmentRequest req = new AppointmentRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setTrn("123456789");
        req.setAppointmentDate(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Appointment saved = service.schedule(req);
        Assertions.assertNotNull(saved.getId());
    }

    @Test
    void testDuplicateAppointmentThrows() {
        AppointmentRequest req = new AppointmentRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setTrn("987654321");
        String futureDate = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        req.setAppointmentDate(futureDate);

        service.schedule(req);

        ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> service.schedule(req));
        Assertions.assertTrue(ex.getMessage().contains("Duplicate appointment"));
    }
}
