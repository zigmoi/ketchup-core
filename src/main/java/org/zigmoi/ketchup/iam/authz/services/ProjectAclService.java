package org.zigmoi.ketchup.iam.authz.services;

import org.zigmoi.ketchup.iam.authz.dtos.ProjectAclDto;
import org.zigmoi.ketchup.project.entities.ProjectId;

public interface ProjectAclService {

     void assignPermission(ProjectAclDto projectAclDto);
     void revokePermission(ProjectAclDto projectAclDto);
     boolean hasProjectPermission(String identity, String permission, ProjectId projectId);
}
