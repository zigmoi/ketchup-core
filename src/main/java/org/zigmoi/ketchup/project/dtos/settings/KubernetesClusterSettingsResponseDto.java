package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;

@Data
public class KubernetesClusterSettingsResponseDto {

    private String projectResourceId;
    private String settingResourceId;
    private String displayName;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;

    private String kubeconfig;
    private String baseAddress;
}
