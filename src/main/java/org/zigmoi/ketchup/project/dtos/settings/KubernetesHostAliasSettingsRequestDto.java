package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Map;

@Data
public class KubernetesHostAliasSettingsRequestDto {

    private String projectResourceId;
    private String displayName;

    private Map<String, String> hostnameIpMapping;
}
