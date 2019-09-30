package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Date;

@Data
public class BuildToolSettingsDto {

    private String projectId;
    private String settingId;
    private String displayName;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;

    private String provider;
    private String fileName;
    private String fileData;
}
