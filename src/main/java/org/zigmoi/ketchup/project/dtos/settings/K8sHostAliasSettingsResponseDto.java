package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class K8sHostAliasSettingsResponseDto {

    private String projectId;
    private String settingId;
    private String displayName;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;

    private Map<String, String> hostnameIpMapping;
}
