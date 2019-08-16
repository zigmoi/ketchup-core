package org.zigmoi.ketchup.iam.dtos;

import lombok.Data;

@Data
public class CloudCredentialDto {
    private String provider;
    private String displayName;
    private String accessId, secretKey;
}
