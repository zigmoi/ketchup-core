package org.zigmoi.ketchup.iam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserResponseDto {
    private String userName; //fully qualified username user@tenant example: test@zigmoi.com
    private String displayName;
    private boolean enabled;
    private String email;
    private String firstName;
    private String lastName;
//    private Date creationDate;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
    Set<String> roles = new HashSet<>();
}
