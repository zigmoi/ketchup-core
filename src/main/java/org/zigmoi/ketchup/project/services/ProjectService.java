package org.zigmoi.ketchup.project.services;

import org.zigmoi.ketchup.project.entities.ProjectId;

import java.util.Set;

public interface ProjectService {

//    boolean verifyMemberExists(ProjectId projectId, String member);
//
//    void addMember(ProjectId projectId, String member);
//
//    void removeMember(ProjectId projectId, String member);

    Set<ProjectId> findAllProjectIds();
}
