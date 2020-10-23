package org.zigmoi.ketchup.application.dtos;

import lombok.Data;

@Data
public class GitRepoConnectionTestRequestDto {
    private String repoUrl;
    private  String username;
    private String password;
}
