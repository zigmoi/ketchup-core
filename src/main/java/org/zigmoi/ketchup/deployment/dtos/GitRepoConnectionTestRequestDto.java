package org.zigmoi.ketchup.deployment.dtos;

import lombok.Data;

@Data
public class GitRepoConnectionTestRequestDto {
    private String repoUrl;
    private  String username;
    private String password;
}
