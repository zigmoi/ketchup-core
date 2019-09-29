package org.zigmoi.ketchup.iam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserDto {
    private String userName; //fully qualified username user@tenant example: test@zigmoi.com
    private String displayName;
    private boolean enabled;
    private String email;
    private String firstName;
    private String lastName;
    private Date creationDate;
    Set<String> roles = new HashSet<>();
}
