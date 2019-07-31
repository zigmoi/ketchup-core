package org.zigmoi.ketchup.iam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserRequestDto {
    private String userName; //fully qualified username user@tenant example: test@zigmoi.com
    private String password;
    private String displayName;
    private boolean enabled;
    private String email;
    private String firstName;
    private String lastName;
    Set<String> roles = new HashSet<>();
}