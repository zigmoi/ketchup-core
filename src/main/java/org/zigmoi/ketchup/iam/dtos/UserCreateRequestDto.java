package org.zigmoi.ketchup.iam.dtos;

import lombok.Getter;
import lombok.Setter;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;

import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserCreateRequestDto {
    @NotBlank(message = "Please provide fully qualified user name with tenant id, example: user@tenant_id.")
    @Size(max = 100)
    private String userName; //fully qualified username user@tenant example: test@zigmoi.com

    @NotBlank
    @Size(max = 100)
    private String password;

    @ValidDisplayName
    private String displayName;

    @NotNull
    private boolean enabled;

    @NotNull
    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @NotNull
    @NotEmpty
    Set<@NotBlank @Size(max = 100) String> roles = new HashSet<>();
}