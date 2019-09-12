package org.zigmoi.ketchup.tenantsetting.dtos;

import lombok.Data;

@Data
public class GitProviderSettingDto {
    private String provider;
    private String repoListUrl, displayName;
    private String username, password;
}
