package jm.gov.mtw.controllers;

import jakarta.validation.Valid;
import jm.gov.mtw.dto.AppointmentRequest;
import jm.gov.mtw.models.Appointment;
import jm.gov.mtw.services.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = service.schedule(request);
        return new ResponseEntity<>(appointment, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> searchAppointments(
            @RequestParam(required = false) String trn,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        List<Appointment> appointmentList = service.searchAppointments(trn, fromDate, toDate);
        return new ResponseEntity<>(appointmentList, HttpStatus.OK);
    }

    @GetMapping("/search")
    public List<Appointment> searchByTrn(@RequestParam String trn) {
        return service.findByTrn(trn);
    }

    @GetMapping("/filter")
    public List<Appointment> filterByDate(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return service.findByDate(parsedDate);
    }
}