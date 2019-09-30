package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;

@Data
public class ContainerRegistrySettingsDto {
    private String projectId;
    private String provider;
    private String displayName;
    private String cloudCredentialId;
    private String registryId, registryUrl;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
}
