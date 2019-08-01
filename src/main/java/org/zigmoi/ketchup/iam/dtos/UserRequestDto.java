package org.zigmoi.ketchup.iam.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserRequestDto {
    @NotBlank(message = "Please provide fully qualified user name with Organization Id, example: user@organization-id.")
    private String userName; //fully qualified username user@tenant example: test@zigmoi.com
    private String password;
    private String displayName;
    private boolean enabled;
    private String email;
    private String firstName;
    private String lastName;
    Set<String> roles = new HashSet<>();
}