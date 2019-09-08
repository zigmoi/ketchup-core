package org.zigmoi.ketchup.project.services;

<<<<<<< HEAD
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;

import javax.swing.text.html.Option;
import java.util.Optional;
=======
import org.zigmoi.ketchup.project.entities.ProjectId;

>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
import java.util.Set;

public interface ProjectService {

<<<<<<< HEAD
    //    boolean verifyMemberExists(ProjectId projectId, String member);
//
    void addMember(ProjectId projectId, String member);

    void removeMember(ProjectId projectId, String member);

    Set<String> listMembers(ProjectId projectId);

    Set<ProjectId> findAllProjectIds();

    Optional<Project> findById(ProjectId projectId);

    boolean validateProject(ProjectId projectId);
=======
//    boolean verifyMemberExists(ProjectId projectId, String member);
//
//    void addMember(ProjectId projectId, String member);
//
//    void removeMember(ProjectId projectId, String member);

    Set<ProjectId> findAllProjectIds();
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
}
