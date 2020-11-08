package org.zigmoi.ketchup.iam.dtos;

import lombok.Getter;
import lombok.Setter;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidTenantId;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class TenantDto {

    @ValidTenantId
    private String id;

    @ValidDisplayName
    private String displayName;

    @NotBlank
    @Size(max = 100)
    private String defaultUserPassword;

    @NotNull
    @Email
    @Size(max = 100)
    private String defaultUserEmail;
}
