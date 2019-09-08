package org.zigmoi.ketchup.project.services;

import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.Set;

public interface ProjectService {

    //    boolean verifyMemberExists(ProjectId projectId, String member);
//
    void addMember(ProjectId projectId, String member);

    void removeMember(ProjectId projectId, String member);

    Set<String> listMembers(ProjectId projectId);

    Set<ProjectId> findAllProjectIds();

    Optional<Project> findById(ProjectId projectId);

    boolean validateProject(ProjectId projectId);
}
