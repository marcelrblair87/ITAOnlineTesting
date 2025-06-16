package jm.gov.mtw.services;

import jakarta.validation.ValidationException;
import jm.gov.mtw.dto.AppointmentRequest;
import jm.gov.mtw.models.Appointment;
import jm.gov.mtw.models.User;
import jm.gov.mtw.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository repository;

    private static final List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    private LocalDateTime parseDate(String input) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(input, formatter);
            } catch (Exception ignored) {}
        }
        throw new ValidationException("Invalid date format. Supported formats: yyyy-MM-dd HH:mm:ss, yyyy/MM/dd HH:mm, ISO");
    }
    public Appointment schedule(AppointmentRequest request) {

        LocalDateTime appointmentDate = parseDate(request.getAppointmentDate());

        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Appointment date must be in the future.");
        }

        boolean exists = repository.findByUser_TrnAndAppointmentDateTime(request.getTrn(), appointmentDate).isPresent();
        if (exists) {
            throw new ValidationException("Duplicate appointment: this TRN already has an appointment at this time.");
        }

        Appointment appointment = new Appointment();
        User user = new User();
        //ToDo: need to flesh out the appointment request object
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTrn(request.getTrn());
        appointment.setUser(user);
        appointment.setAppointmentDateTime(appointmentDate);

        return repository.save(appointment);
    }


    public List<Appointment> searchAppointments(String trn, String fromDate, String toDate) {
        if (trn != null) {
            return repository.findByUser_Trn(trn);
        } else if (fromDate != null && toDate != null) {
            LocalDateTime from = parseDate(fromDate);
            LocalDateTime to = parseDate(toDate);
            return repository.findByAppointmentDateTimeBetween(from, to);
        } else {
            return repository.findAll();
        }
    }

    public List<Appointment> findByTrn(String trn) {
        return repository.findByUser_Trn(trn);
    }

    public List<Appointment> findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return repository.findByAppointmentDateTimeBetween(start, end);
    }
}