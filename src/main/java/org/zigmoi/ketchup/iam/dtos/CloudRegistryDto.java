package org.zigmoi.ketchup.iam.dtos;

import lombok.Data;

@Data
public class CloudRegistryDto {
    private String provider;
    private String displayName;
    private String cloudCredentialId;
    private String registryId, registryUrl;
}
