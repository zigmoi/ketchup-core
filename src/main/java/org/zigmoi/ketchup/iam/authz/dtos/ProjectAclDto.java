package org.zigmoi.ketchup.iam.authz.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class ProjectAclDto {
    private String identity;
    private String resourceId;
    private Set<String> permissions;
}
