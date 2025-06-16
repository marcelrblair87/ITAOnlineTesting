package jm.gov.mtw.services;

import jm.gov.mtw.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    public boolean verifyUser(String trn, String firstName, String lastName) {
        return appointmentRepository.existsByUserInfo(trn, firstName, lastName);
    }
}
