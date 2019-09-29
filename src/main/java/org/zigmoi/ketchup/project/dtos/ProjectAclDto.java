package org.zigmoi.ketchup.project.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class ProjectAclDto {
    private String identity;
    private String projectResourceId;
    private Set<String> permissions;
}
