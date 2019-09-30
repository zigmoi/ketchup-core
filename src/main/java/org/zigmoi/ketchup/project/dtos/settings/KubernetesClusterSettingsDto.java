package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;

@Data
public class KubernetesClusterSettingsDto {
    private String projectId;
    private String provider;
    private String fileName, fileData;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
}
