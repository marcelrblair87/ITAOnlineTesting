package jm.gov.mtw.repository;


import jm.gov.mtw.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByUser_TrnAndAppointmentDateTime(String trn, LocalDateTime appointmentDate);
    List<Appointment> findByUser_Trn(String trn);
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
    SELECT COUNT(a) > 0 FROM Appointment a
    WHERE LOWER(a.user.firstName) = LOWER(:firstName)
      AND LOWER(a.user.lastName) = LOWER(:lastName)
      AND a.user.trn = :trn
    """)
    boolean existsByUserInfo(String trn, String firstName, String lastName);
}
