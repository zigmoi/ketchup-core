package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

@Data
public class GitProviderSettingsRequestDto {

    private String projectId;
    private String displayName;

    private String provider;
    private String repoListUrl;
    private String username;
    private String password;
}
