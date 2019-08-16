package org.zigmoi.ketchup.iam.dtos;

import lombok.Data;

@Data
public class BuildToolDto {
    private String provider;
    private String displayName;
    private String fileName, fileRemoteUrl;
}
