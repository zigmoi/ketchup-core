package org.zigmoi.ketchup.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

//    @Override
//    @Transactional
//    public boolean verifyMemberExists(ProjectId projectId, String member) {
//        return projectRepository.existsByIdAndMembersExists(projectId, member);
//    }
//
//    @Override
//    @Transactional
//    public void addMember(ProjectId projectId, String member) {
//        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ""));
//        Set<String> members = project.getMembers();
//        members.add(member);
//        project.setMembers(members);
//        projectRepository.save(project);
//    }
//
//    @Override
//    @Transactional
//    public void removeMember(ProjectId projectId, String member) {
//
//    }

    @Override
    public Set<ProjectId> findAllProjectIds() {
        return projectRepository.findAllProjectIds();
    }
}
