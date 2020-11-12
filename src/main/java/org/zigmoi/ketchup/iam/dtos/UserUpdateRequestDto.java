package org.zigmoi.ketchup.iam.dtos;

import lombok.Getter;
import lombok.Setter;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;

import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserUpdateRequestDto {
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