package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

@Data
public class CloudProviderSettingsRequestDto {

    private String projectId;
    private String displayName;

    private String provider;
    private String accessId;
    private String secretKey;
}
