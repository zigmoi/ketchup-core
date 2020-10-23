package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

@Data
public class ContainerRegistrySettingsRequestDto {

    private String projectResourceId;
    private String displayName;

    private String type;
    private String registryUrl;
    private String repository; //project id for gcr
    private String registryUsername;
    private String registryPassword;
}
