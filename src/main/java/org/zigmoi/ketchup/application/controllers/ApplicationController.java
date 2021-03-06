package org.zigmoi.ketchup.application.controllers;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.application.dtos.*;
import org.zigmoi.ketchup.application.entities.Application;
import org.zigmoi.ketchup.application.entities.ApplicationId;
import org.zigmoi.ketchup.application.entities.Revision;
import org.zigmoi.ketchup.application.services.ApplicationService;
import org.zigmoi.ketchup.application.services.DeploymentTriggerType;
import org.zigmoi.ketchup.application.services.PlatformConfigReader;
import org.zigmoi.ketchup.common.GitWebHookParserUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.common.validations.ValidResourceId;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Validated
@Slf4j
@RestController
@RequestMapping("/v1-alpha/projects/{project-resource-id}/applications")
public class ApplicationController {
    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    @Autowired
    private Validator validator;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PermissionUtilsService permissionUtilsService;

    @PostMapping
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public void createApplication(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                  @RequestBody ApplicationRequestDto applicationRequestDto) {
        Set<ConstraintViolation<ApplicationRequestDto>> violations = validator.validate(applicationRequestDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (!PlatformConfigReader.instance().getPlatformConfig().isValid(applicationRequestDto.getPlatform(), applicationRequestDto.getBuildTool())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid mapping for platform %s and build tool %s", applicationRequestDto.getPlatform(), applicationRequestDto.getBuildTool()));
        }
        applicationService.createApplication(projectResourceId, applicationRequestDto);
    }

    @GetMapping("/{application-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public ApplicationResponseDto getApplication(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                 @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                 @RequestParam(name = "live-status", required = false, defaultValue = "false") boolean liveStatus) {
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        ApplicationDetailsDto application = applicationService.getApplication(id);
        ModelMapper modelMapper = new ModelMapper();
        ApplicationResponseDto response = modelMapper.map(application, ApplicationResponseDto.class);
        if (liveStatus) {
            DeploymentStatus status = applicationService.getLiveStatusForApplication(id);
            response.setDeploymentStatus(status);
        }
        return response;
    }

    @PutMapping("/{application-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public void updateApplication(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                  @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                  @RequestBody ApplicationRequestDto applicationRequestDto) {
        //TODO provide option to update current application with new changes.
        Set<ConstraintViolation<ApplicationRequestDto>> violations = validator.validate(applicationRequestDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (!PlatformConfigReader.instance().getPlatformConfig().isValid(applicationRequestDto.getPlatform(), applicationRequestDto.getBuildTool())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid mapping for platform %s and build tool %s", applicationRequestDto.getPlatform(), applicationRequestDto.getBuildTool()));
        }
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        applicationService.updateApplication(id, applicationRequestDto);
    }

    @DeleteMapping("/{application-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteApplication(#projectResourceId)")
    public void deleteApplication(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                  @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                  @RequestParam (required = false) Boolean force) {
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        if (force == null){
            force = Boolean.FALSE;
        }
        applicationService.deleteApplication(id, force);
    }

    @GetMapping
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public List<ApplicationBasicResponseDto> listAllApplicationsInProject(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                                          @RequestParam (required = false) Boolean full) {
        if (full == null) full = Boolean.TRUE;
        return applicationService.listAllApplicationsInProject(projectResourceId, full);
    }

    @GetMapping("/{application-resource-id}/instances")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public List<String> getApplicationInstances(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId) {
        //TODO find active version of application and use its kubeconfig to get instances.
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        ApplicationDetailsDto applicationDetailsDto = applicationService.getApplication(id);
        String kubeConfig = StringUtility.decodeBase64(applicationDetailsDto.getDevKubeconfig());
        try {
            String labelSelector = "app.kubernetes.io/instance=app-".concat(applicationResourceId);
            V1PodList res = KubernetesUtility.listPods(labelSelector, applicationDetailsDto.getDevKubernetesNamespace(), "false", kubeConfig);
            System.out.println(res);
            return res.getItems().stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.toList());
        } catch (IOException | ApiException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get instances.");
        }
    }

    @GetMapping("/{application-resource-id}/current-revision")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Revision getCurrentRevision(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                       @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId) {
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        return applicationService.getCurrentRevision(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current revision not found."));
    }

    @GetMapping("/{application-resource-id}/last-successful-revision")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Revision getLastSuccessfulRevision(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                              @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId) {
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        return applicationService.getLastSuccessfulRevision(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Last successful revision not found."));
    }

    @PostMapping(value = "/{application-resource-id}/git-webhook/listen")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public void handleGitWebHookRequests(@PathVariable("project-resource-id") String projectResourceId,
                                         @PathVariable("application-resource-id") String applicationResourceId,
                                         @RequestParam("vendor") String vendor,
                                         @RequestBody(required = false) String req) {
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        ApplicationDetailsDto applicationDetailsDto = applicationService.getApplication(id);
        if (applicationDetailsDto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        GitWebHookParserUtility.WebhookEvent event = GitWebHookParserUtility.parseEvent(vendor, applicationResourceId, req);
        log.info("Received Webhook Event : " + event.toString());

        nonBlockingService.submit(() -> {
            if (GitWebHookParserUtility.isPushEvent(event)
                    && applicationDetailsDto.getGitRepoBranchName().equalsIgnoreCase(event.getBranchName())) {
                try {
                    String commitId = event.getCommitId();
                    applicationService.createRevision(DeploymentTriggerType.GIT_WEBHOOK.toString(), commitId, id);
                    log.info("Invoked redeploy for Webhook Event : " + event.toString());
                } catch (Exception e) {
                    log.error("Failed redeploy for Webhook Event : " + event.toString(), e);
                }
            } else {
                log.info("Ignored Webhook Event : " + event.toString());
            }
        });
    }

    @GetMapping(value = "/{application-resource-id}/git-webhook/generate/listener-url")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Map<String, String> generateGitWebHookListenerURL(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                             @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                             @RequestParam("vendor") @NotBlank String vendor) {
        ApplicationId id = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        String url = applicationService.generateGitWebhookListenerURL(vendor, id);
        Map<String, String> map = new HashMap<>();
        map.put("webhookUrl", url);
        return map;
    }
}
