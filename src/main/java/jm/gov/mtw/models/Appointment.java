package jm.gov.mtw.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_trn", referencedColumnName = "trn")
    private User user;

    private LocalDateTime appointmentDateTime;
    private String location; // e.g., service hub - kingston, spanish town, etc.
}