package org.zigmoi.ketchup.tenantsetting.dtos;

import lombok.Data;

@Data
public class CloudRegistrySettingDto {
    private String provider;
    private String displayName;
    private String cloudCredentialId;
    private String registryId, registryUrl;
}
