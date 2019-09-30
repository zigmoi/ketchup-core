package org.zigmoi.ketchup.deployment.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentDto;
import org.zigmoi.ketchup.deployment.repositories.DeploymentAclRepository;
import org.zigmoi.ketchup.deployment.repositories.DeploymentRepository;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.project.services.ProjectService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeploymentServiceImpl implements DeploymentService {

    private final DeploymentAclRepository deploymentAclRepository;
    private final DeploymentRepository deploymentRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;

    @Autowired
    public DeploymentServiceImpl(DeploymentAclRepository deploymentAclRepository, DeploymentRepository deploymentRepository) {
        this.deploymentAclRepository = deploymentAclRepository;
        this.deploymentRepository = deploymentRepository;
    }

    @Override
    @Transactional
    public void createBasicSpringBootDeployment(BasicSpringBootDeploymentDto basicSpringBootDeploymentDto) {

    }

    @Override
    @Transactional
    public void updateDeploymentStatus(String deploymentId, String status) {

    }

    @Override
    @Transactional
    public void updateDeploymentDisplayName(String deploymentId, String displayName) {

    }

    @Override
    @Transactional
    public Optional<BasicSpringBootDeploymentDto> getBasicSpringBootDeployment(String deploymentId) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public void deleteDeployment(String deploymentId) {

    }

    @Override
    @Transactional
    public List<BasicSpringBootDeploymentDto> listAllBasicSpringBootDeployments() {
        return new ArrayList<>();
    }
}
