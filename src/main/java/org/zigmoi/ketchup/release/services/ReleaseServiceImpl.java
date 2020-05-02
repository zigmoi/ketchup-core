package org.zigmoi.ketchup.release.services;

import io.kubernetes.client.openapi.ApiException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.release.entities.PipelineResource;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;
import org.zigmoi.ketchup.release.repositories.ReleaseRepository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        Release r = new Release();
        ReleaseId id = new ReleaseId();
        id.setProjectResourceId("p1");
        id.setDeploymentResourceId("d1");
        id.setReleaseResourceId("r1");
        id.setTenantId("t1.com");
        r.setId(id);

        //generate helm values json and save it.

        //generate pipeline all resources and save them, name can be parsed from them directly.
        List<PipelineResource> pipelineResources = getAllPipelineResources("standard-sb-1");
        r.setPipelineResources(pipelineResources);

        releaseRepository.save(r);

        deployPipelineResources(pipelineResources);

//        //deploy pipeline resources.
//        String baseResourcePath = "/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/";
//        try {
//            //create pipeline resources in order. (createPipelineRun should be last.)
//            KubernetesUtility.createSecret(baseResourcePath.concat("secrets.yaml"));
//            KubernetesUtility.createServiceAccount(baseResourcePath.concat("service-account.yaml"));
//            KubernetesUtility.createCustomResource(baseResourcePath.concat("resource.yaml"), "default", "tekton.dev", "v1alpha1", "pipelineresources", "false");
//            KubernetesUtility.createCustomResource(baseResourcePath.concat("task-makisu.yaml"), "default", "tekton.dev", "v1alpha1", "tasks", "false");
//            KubernetesUtility.createCustomResource(baseResourcePath.concat("task-helm.yaml"), "default", "tekton.dev", "v1alpha1", "tasks", "false");
//            KubernetesUtility.createCustomResource(baseResourcePath.concat("pipeline.yaml"), "default", "tekton.dev", "v1alpha1", "pipelines", "false");
//            KubernetesUtility.createCustomResource(baseResourcePath.concat("pipeline-run.yaml"), "default", "tekton.dev", "v1alpha1", "pipelineruns", "false");
//        } catch (IOException | ApiException e) {
//            //delete all resources.
//            e.printStackTrace();
//        }

    }

    private void deployPipelineResources(List<PipelineResource> pipelineResources) {

        pipelineResources.stream()
                .filter(r -> "secret".equalsIgnoreCase(r.getResourceType()))
                .forEach(secret -> {
                    try {
                        KubernetesUtility.createSecretUsingYamlContent(secret.getResourceContent(), "default", "false");
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
                        KubernetesUtility.createServiceAccountUsingYamlContent(serviceAccount.getResourceContent(), "default", "false");
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
                        KubernetesUtility.createCRDUsingYamlContent(pipelineResource.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelineresources", "false");
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
                        KubernetesUtility.createCRDUsingYamlContent(task.getResourceContent(), "default", "tekton.dev", "v1alpha1", "tasks", "false");
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
                        KubernetesUtility.createCRDUsingYamlContent(pipeline.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelines", "false");
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
                        KubernetesUtility.createCRDUsingYamlContent(pipelineRun.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelineruns", "false");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });


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
