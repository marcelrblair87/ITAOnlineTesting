package jm.gov.mtw.dto;

import jm.gov.mtw.models.User;
import lombok.Data;

@Data
public class UserDto {
    private String trn;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.setTrn(user.getTrn());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }
}