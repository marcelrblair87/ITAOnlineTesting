package jm.gov.mtw.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    private String trn;
    private String firstName;
    private String lastName;

}
