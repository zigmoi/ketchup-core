package org.zigmoi.ketchup.tenantsetting.dtos;

import lombok.Data;

@Data
public class CloudClusterSettingDto {
    private String provider;
    private String displayName;
    private String fileName, fileRemoteUrl;
}
