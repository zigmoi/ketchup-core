package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

@Data
public class KubernetesClusterSettingsRequestDto {

    private String projectId;
    private String displayName;

    private String fileData;
}
