package jm.gov.mtw.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    private String trn;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    private String streetNumber;
    private String streetName;
    private String addressLine2;
    private String city;
    private String parish;
    private String postalCode;

    private String phoneNumber;
    private String email;
}
