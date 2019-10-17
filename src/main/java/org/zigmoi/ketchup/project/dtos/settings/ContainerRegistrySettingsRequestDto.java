package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

@Data
public class ContainerRegistrySettingsRequestDto {

    private String projectId;
    private String displayName;

    private String provider;
    private String cloudCredentialId;
    private String registryId;
    private String registryUrl;
}
