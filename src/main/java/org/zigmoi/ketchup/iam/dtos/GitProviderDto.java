package org.zigmoi.ketchup.iam.dtos;

import lombok.Data;

@Data
public class GitProviderDto {
    private String provider;
    private String repoListUrl, displayName;
    private String username, password;
}
