package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class HostnameIpMappingSettingsDto {
    private String projectId;
    private Map<String, String> hostnameIPMapping;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
}
