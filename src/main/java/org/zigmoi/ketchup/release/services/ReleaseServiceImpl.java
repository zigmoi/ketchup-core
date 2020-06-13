package org.zigmoi.ketchup.release.services;

import io.kubernetes.client.openapi.ApiException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.release.entities.PipelineResource;
import org.zigmoi.ketchup.release.entities.PipelineResourceId;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;
import org.zigmoi.ketchup.release.repositories.PipelineResourceRepository;
import org.zigmoi.ketchup.release.repositories.ReleaseRepository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ReleaseServiceImpl extends TenantProviderService implements ReleaseService {

    private final ReleaseRepository releaseRepository;

    private final PipelineResourceRepository pipelineResourceRepository;

    private PermissionUtilsService permissionUtilsService;

    @Autowired
    public ReleaseServiceImpl(ReleaseRepository releaseRepository, PipelineResourceRepository pipelineResourceRepository, PermissionUtilsService permissionUtilsService) {
        this.releaseRepository = releaseRepository;
        this.pipelineResourceRepository = pipelineResourceRepository;
        this.permissionUtilsService = permissionUtilsService;
    }


    @Override
    @Transactional
    public void create(String deploymentResourceId) {
        //validate and fetch deploymentId.
        //get projectId from deployment.
        //handle duplicate pipeline resources error in k8s.

        String tenantId = AuthUtils.getCurrentTenantId();
        String projectResourceId = "p1";


        Release r = new Release();
        ReleaseId releaseId = new ReleaseId();
        releaseId.setReleaseResourceId(UUID.randomUUID().toString());
        releaseId.setTenantId(tenantId);
        r.setId(releaseId);
        r.setDeploymentResourceId(deploymentResourceId);
        r.setProjectResourceId(projectResourceId);

        //generate helm values json and save it.

        //generate pipeline all resources and save them, name can be parsed from them directly.
        List<PipelineResource> pipelineResources = getAllPipelineResources("standard-sb-1");
        pipelineResources.stream().forEach(pipelineResource ->
        {
            PipelineResourceId pipelineResourceId = new PipelineResourceId();
            pipelineResourceId.setTenantId(tenantId);
            pipelineResourceId.setGuid(UUID.randomUUID().toString());
            pipelineResource.setId(pipelineResourceId);
            pipelineResource.setProjectResourceId(projectResourceId);
            pipelineResource.setReleaseResourceId(releaseId.getReleaseResourceId());
        });
        pipelineResourceRepository.saveAll(pipelineResources);
        releaseRepository.save(r);

        deployPipelineResources(pipelineResources, "/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
    }

    @Override
    @Transactional
    public void rollback(String deploymentResourceId) {

    }

    @Override
    @Transactional
    public void stop(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);

        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Release with id %s not found.", releaseResourceId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Release findById(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);
        return releaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Release with id %s not found.", releaseResourceId)));
    }

    @Override
    @Transactional
    public void delete(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);

        if (releaseRepository.existsById(id) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Release with id %s not found.", releaseResourceId));
        }

        releaseRepository.deleteById(id);
        pipelineResourceRepository.deleteAllByReleaseResourceId(releaseResourceId);
    }

    @Override
    @Transactional
    public void update(Release release) {

    }

    @Override
    @Transactional
    public Set<Release> listAllInDeployment(String deploymentResourceId) {
        return releaseRepository.findDistinctByDeploymentResourceIdOrderByCreatedOnDesc(deploymentResourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Release> listAllInProject(String projectResourceId) {
        return releaseRepository.findDistinctByProjectResourceId(projectResourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PipelineResource> listAllPipelineResources(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);

        if (releaseRepository.existsById(id) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Release with id %s not found.", releaseResourceId));
        }
        return pipelineResourceRepository.findDistinctByReleaseResourceId(releaseResourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public PipelineResource getPipelineResourceById(String pipelineResourceId) {
        PipelineResourceId id = new PipelineResourceId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setGuid(pipelineResourceId);
        return pipelineResourceRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format("Pipeline Resource with id %s not found.", pipelineResourceId)));
    }


    private void deployPipelineResources(List<PipelineResource> pipelineResources, String kubeConfigPath) {
        pipelineResources.stream()
                .filter(r -> "secret".equalsIgnoreCase(r.getResourceType()))
                .forEach(secret -> {
                    try {
                        KubernetesUtility.createSecretUsingYamlContent(secret.getResourceContent(), "default", "false", kubeConfigPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        //check service-account is not more than one.
        pipelineResources.stream()
                .filter(r -> "service-account".equalsIgnoreCase(r.getResourceType()))
                .forEach(serviceAccount -> {
                    try {
                        KubernetesUtility.createServiceAccountUsingYamlContent(serviceAccount.getResourceContent(), "default", "false", kubeConfigPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "pipeline-resource".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineResource -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipelineResource.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelineresources", "false", kubeConfigPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "task".equalsIgnoreCase(r.getResourceType()))
                .forEach(task -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(task.getResourceContent(), "default", "tekton.dev", "v1alpha1", "tasks", "false", kubeConfigPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "pipeline".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipeline -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipeline.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelines", "false", kubeConfigPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        //check pipeline-run is not more than one.
        pipelineResources.stream()
                .filter(r -> "pipeline-run".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineRun -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipelineRun.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelineruns", "false", kubeConfigPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });


    }


    private List<PipelineResource> getAllPipelineResources(String pipelineType) {
        String baseResourcePath = "/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/";

        List<PipelineResource> resources = new ArrayList<>();

        PipelineResource r1 = new PipelineResource();
        r1.setFormat("yaml");
        r1.setResourceType("secret");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("secrets.yaml")), StandardCharsets.UTF_8);
            r1.setResourceContent(content);
            resources.add(r1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r2 = new PipelineResource();
        r2.setFormat("yaml");
        r2.setResourceType("service-account");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("service-account.yaml")), StandardCharsets.UTF_8);
            r2.setResourceContent(content);
            resources.add(r2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r3 = new PipelineResource();
        r3.setFormat("yaml");
        r3.setResourceType("pipeline-resource");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("resource.yaml")), StandardCharsets.UTF_8);
            r3.setResourceContent(content);
            resources.add(r3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r4 = new PipelineResource();
        r4.setFormat("yaml");
        r4.setResourceType("task");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("task-makisu.yaml")), StandardCharsets.UTF_8);
            r4.setResourceContent(content);
            resources.add(r4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r5 = new PipelineResource();
        r5.setFormat("yaml");
        r5.setResourceType("task");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("task-helm.yaml")), StandardCharsets.UTF_8);
            r5.setResourceContent(content);
            resources.add(r5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r6 = new PipelineResource();
        r6.setFormat("yaml");
        r6.setResourceType("pipeline");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("pipeline.yaml")), StandardCharsets.UTF_8);
            r6.setResourceContent(content);
            resources.add(r6);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r7 = new PipelineResource();
        r7.setFormat("yaml");
        r7.setResourceType("pipeline-run");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("pipeline-run.yaml")), StandardCharsets.UTF_8);
            r7.setResourceContent(content);
            resources.add(r7);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resources;
    }
}
