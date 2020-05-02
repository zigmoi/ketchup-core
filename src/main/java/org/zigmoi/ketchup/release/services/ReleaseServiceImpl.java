package org.zigmoi.ketchup.release.services;

import io.kubernetes.client.openapi.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.project.dtos.ProjectDto;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;
import org.zigmoi.ketchup.release.repositories.ReleaseRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReleaseServiceImpl extends TenantProviderService implements ReleaseService {

    private final ReleaseRepository releaseRepository;

    private PermissionUtilsService permissionUtilsService;

    private UserService userService;

    @Autowired
    public ReleaseServiceImpl(ReleaseRepository releaseRepository, PermissionUtilsService permissionUtilsService, UserService userService) {
        this.releaseRepository = releaseRepository;
        this.permissionUtilsService = permissionUtilsService;
        this.userService = userService;
    }


    @Override
    @Transactional
    public void create() {
        //generate helm values json.
        //generate pipeline all resources.
        //prepare release entity and save it.
        //deploy pipeline resources.
        //create pipeline resources in order. (createPipelineRun should be last.)
        Release r = new Release();
        ReleaseId id = new ReleaseId();
        id.setProjectResourceId("p1");
        id.setDeploymentResourceId("d1");
        id.setReleaseResourceId("r1");
        id.setTenantId("t1.com");
        r.setId(id);

        String baseResourcePath = "/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/";
        try {
            r.setPipelineResourceIds(KubernetesUtility.createPipelineResource(baseResourcePath.concat("resource.yaml")));
            String taskName1 = KubernetesUtility.createPipelineTask(baseResourcePath.concat("task-makisu.yaml"));
            String taskName2 = KubernetesUtility.createPipelineTask(baseResourcePath.concat("task-helm.yaml"));
            r.setPipelineTaskIds(taskName1.concat(",").concat(taskName2));
            r.setPipelineSecretIds(KubernetesUtility.createSecret(baseResourcePath.concat("secrets.yaml")));
            r.setPipelineServiceAccountIds(KubernetesUtility.createServiceAccount(baseResourcePath.concat("service-account.yaml")));
            r.setPipelineId(KubernetesUtility.createPipeline(baseResourcePath.concat("pipeline.yaml")));
            r.setPipelineRunId(KubernetesUtility.createPipelineRun(baseResourcePath.concat("pipeline-run.yaml")));
            releaseRepository.save(r);
        } catch (IOException | ApiException e) {
            //delete all resources.
            e.printStackTrace();
        }

    }

    @Override
    @Transactional
    public void stop(String releaseResourceId) {

    }

    @Override
    @Transactional
    public Optional<Release> findById(String releaseResourceId) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public void delete(String releaseResourceId) {

    }

    @Override
    @Transactional
    public void update(Release release) {

    }

    @Override
    @Transactional
    public Set<Release> listAllInDeployment() {
        return null;
    }

    @Override
    @Transactional
    public Set<Release> listAllInProject() {
        return null;
    }
}
