package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;

@Data
public class CloudProviderSettingsDto {
    private String projectId;
    private String provider;
    private String displayName;
    private String accessId;
    private String secretKey;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
}
