package jm.gov.mtw.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String message;

    public LoginResponse(String message) {
        this.message = message;
    }

}