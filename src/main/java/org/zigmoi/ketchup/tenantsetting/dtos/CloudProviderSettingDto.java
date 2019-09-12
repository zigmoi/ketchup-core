package org.zigmoi.ketchup.tenantsetting.dtos;

import lombok.Data;

@Data
public class CloudProviderSettingDto {
    private String provider;
    private String displayName;
    private String accessId, secretKey;
}
