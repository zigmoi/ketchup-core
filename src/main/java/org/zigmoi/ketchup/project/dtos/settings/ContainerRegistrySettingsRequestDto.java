package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

@Data
public class ContainerRegistrySettingsRequestDto {

    private String projectId;
    private String displayName;

    private String type;
    private String registryUrl;
    private String registryUsername;
    private String registryPassword;
}
