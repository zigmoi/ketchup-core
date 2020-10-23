package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class KubernetesHostAliasSettingsResponseDto {

    private String projectResourceId;
    private String settingResourceId;
    private String displayName;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;

    private Map<String, String> hostnameIpMapping;
}
