package org.zigmoi.ketchup.iam.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantDto {

    private String id;
    private String displayName;
    private String defaultUserPassword;
    private String defaultUserEmail;
}
