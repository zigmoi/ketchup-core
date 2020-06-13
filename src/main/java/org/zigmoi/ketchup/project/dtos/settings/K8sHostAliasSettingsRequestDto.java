package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;

import java.util.Map;

@Data
public class K8sHostAliasSettingsRequestDto {

    private String projectId;
    private String displayName;

    private Map<String, String> hostnameIpMapping;
}
