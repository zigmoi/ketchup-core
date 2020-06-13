package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

@Data
public class BuildToolSettingsRequestDto {

    private String projectId;
    private String displayName;

    private String type;
    private String fileData;
}
