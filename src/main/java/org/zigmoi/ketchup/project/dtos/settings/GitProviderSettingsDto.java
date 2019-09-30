package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;

@Data
public class GitProviderSettingsDto {
    private String projectId;
    private String provider;
    private String repoListUrl, displayName;
    private String username, password;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
}
