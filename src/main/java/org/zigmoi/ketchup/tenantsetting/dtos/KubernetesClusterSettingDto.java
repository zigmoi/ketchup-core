package org.zigmoi.ketchup.tenantsetting.dtos;

import lombok.Data;

@Data
public class KubernetesClusterSettingDto {
    private String provider;
    private String repoListUrl, displayName;
    private String username, password;
}
