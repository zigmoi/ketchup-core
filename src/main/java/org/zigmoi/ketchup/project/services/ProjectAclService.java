package org.zigmoi.ketchup.project.services;

import org.zigmoi.ketchup.project.dtos.ProjectAclDto;
import org.zigmoi.ketchup.project.entities.ProjectId;

public interface ProjectAclService {

     void assignPermission(ProjectAclDto projectAclDto);
     void revokePermission(ProjectAclDto projectAclDto);
     boolean hasProjectPermission(String identity, String permission, ProjectId projectId);
}
