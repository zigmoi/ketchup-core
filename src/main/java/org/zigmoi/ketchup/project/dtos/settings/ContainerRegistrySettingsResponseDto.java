package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;

@Data
public class ContainerRegistrySettingsResponseDto {

    private String projectResourceId;
    private String settingResourceId;
    private String displayName;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;

    private String type;
    private String registryUrl;
    private String repository; //project id for gcr
    private String registryUsername;
    private String registryPassword;

}
