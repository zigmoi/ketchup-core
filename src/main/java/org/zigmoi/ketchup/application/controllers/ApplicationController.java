package org.zigmoi.ketchup.application.controllers;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.application.dtos.ApplicationDetailsDto;
import org.zigmoi.ketchup.application.dtos.ApplicationRequestDto;
import org.zigmoi.ketchup.application.dtos.ApplicationResponseDto;
import org.zigmoi.ketchup.application.entities.Application;
import org.zigmoi.ketchup.application.entities.ApplicationId;
import org.zigmoi.ketchup.application.entities.Revision;
import org.zigmoi.ketchup.application.entities.RevisionId;
import org.zigmoi.ketchup.application.services.ApplicationService;
import org.zigmoi.ketchup.common.GitWebHookParserUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.iam.commons.AuthUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1-alpha/projects/{project-resource-id}/applications")
public class ApplicationController {
    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    public void createApplication(@RequestBody ApplicationRequestDto applicationRequestDto,
                                  @PathVariable("project-resource-id") String projectResourceId) {
        applicationService.createDeployment(projectResourceId, applicationRequestDto);
    }

    @GetMapping("/{application-resource-id}")
    public ApplicationResponseDto getApplication(@PathVariable("project-resource-id") String projectResourceId,
                                                 @PathVariable("application-resource-id") String applicationResourceId) {
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        return applicationService.getDeploymentDetails(id);
    }

    @PutMapping("/{application-resource-id}")
    public void updateApplication(@PathVariable("project-resource-id") String projectResourceId,
                                  @PathVariable("application-resource-id") String applicationResourceId,
                                  @RequestBody ApplicationRequestDto applicationRequestDto) {
        //TODO provide option to update current deployment with new changes.
        applicationService.updateDeployment(projectResourceId, applicationResourceId, applicationRequestDto);
    }

    @DeleteMapping("/{application-resource-id}")
    public void deleteApplication(@PathVariable("project-resource-id") String projectResourceId,
                                  @PathVariable("application-resource-id") String applicationResourceId) {
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        applicationService.deleteDeployment(id);
    }

    @GetMapping
    public List<Application> listAllApplicationsInProject(@PathVariable("project-resource-id") String projectResourceId) {
        return applicationService.listAllDeployments(projectResourceId);
    }

    @GetMapping("/{application-resource-id}/rollback")
    public void rollbackRelease(@PathVariable("project-resource-id") String projectResourceId,
                                @PathVariable("application-resource-id") String applicationResourceId,
                                @RequestParam("to-revision-resource-id") String toRevisionResourceId) {
        //rollback current application to release version toReleaseResourceId.
        RevisionId id = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, toRevisionResourceId);
        applicationService.rollback(id);
    }

    @GetMapping("/{application-resource-id}/instances")
    public List<String> getApplicationInstances(@PathVariable("project-resource-id") String projectResourceId,
                                                @PathVariable("application-resource-id") String applicationResourceId) {
        //TODO find active version of deployment and use its kubeconfig to get instances.
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        ApplicationDetailsDto applicationDetailsDto = applicationService.getDeployment(id);
        String kubeConfig = StringUtility.decodeBase64(applicationDetailsDto.getDevKubeconfig());
        try {
            String labelSelector = "app.kubernetes.io/instance=release-".concat(applicationResourceId);
            V1PodList res = KubernetesUtility.listPods(labelSelector, applicationDetailsDto.getDevKubernetesNamespace(), "false", kubeConfig);
            System.out.println(res);
            return res.getItems().stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.toList());
        } catch (IOException | ApiException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get instances.");
        }
    }

    @GetMapping("/{application-resource-id}/active-revision")
    public Revision getActiveRevision(@PathVariable("project-resource-id") String projectResourceId,
                                      @PathVariable("application-resource-id") String applicationResourceId) {
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        return applicationService.getActiveRelease(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active revision not found."));
    }

    @PostMapping(value = "/{application-resource-id}/git-webhook/listen")
    public void handleGitWebHookRequests(@PathVariable("project-resource-id") String projectResourceId,
                                         @PathVariable("application-resource-id") String applicationResourceId,
                                         @RequestParam("vendor") String vendor,
                                         @RequestBody(required = false) String req) {
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        ApplicationDetailsDto applicationDetailsDto = applicationService.getDeployment(id);
        if (applicationDetailsDto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        GitWebHookParserUtility.WebhookEvent event = GitWebHookParserUtility.parseEvent(vendor, applicationResourceId, req);
        log.info("Received Webhook Event : " + event.toString());

        nonBlockingService.submit(() -> {
            if (GitWebHookParserUtility.isPushEvent(event)
                    && applicationDetailsDto.getGitRepoBranchName().equalsIgnoreCase(event.getBranchName())) {
                try {
                    applicationService.create(id);
                    log.info("Invoked redeploy for Webhook Event : " + event.toString());
                } catch (Exception e) {
                    log.error("Failed redeploy for Webhook Event : " + event.toString(), e);
                }
            } else {
                log.info("Ignored Webhook Event : " + event.toString());
            }
        });
    }

    @GetMapping(value = "/{application-resource-id}/git-webhook/listener-url")
    public Map<String, String> generateGitWebHookListenerURL(@PathVariable("project-resource-id") String projectResourceId,
                                                             @PathVariable("application-resource-id") String applicationResourceId,
                                                             @RequestParam("vendor") String vendor) {
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        String url = applicationService.generateGitWebhookListenerURL(vendor, id);
        Map<String, String> map = new HashMap<>();
        map.put("webhookUrl", url);
        return map;
    }
}
