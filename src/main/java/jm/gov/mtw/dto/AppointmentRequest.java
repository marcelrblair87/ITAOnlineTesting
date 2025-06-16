package jm.gov.mtw.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class AppointmentRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Pattern(regexp = "\\d{9}", message = "TRN must be a 9-digit number")
    private String trn;

    @NotBlank(message = "Appointment date is required (format: yyyy-MM-dd HH:mm:ss)")
    private String appointmentDate;

}