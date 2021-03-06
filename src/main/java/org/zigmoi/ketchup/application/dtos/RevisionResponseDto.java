package org.zigmoi.ketchup.application.dtos;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.zigmoi.ketchup.application.entities.ApplicationId;
import org.zigmoi.ketchup.application.entities.RevisionId;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class RevisionResponseDto {
    private RevisionId revisionId;
    private Date createdOn;
    private String createdBy;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
    private String version;
    private String status;
    private String deploymentTriggerType;
    private String errorMessage;
    private String commitId;
    private String helmChartId;
    private String helmReleaseId;
    private String helmReleaseVersion;
    private boolean rollback;
    private String originalRevisionVersionId;
    private String applicationType;
    private String serviceName;
    private String serviceType;
    private String appServerPort;
    private String replicas;
    private String deploymentStrategy;
    private String gitRepoUrl;
    private String gitRepoUsername;
    private String gitRepoPassword;
    private String gitRepoBranchName;
    private String continuousDeployment;
    private String gitRepoPollingInterval;
    private String platform;
    private String containerRegistrySettingId;
    private String containerRegistryType;
    private String containerRegistryUrl;
    private String containerRegistryUsername;
    private String containerRegistryPassword;
    private String containerRepositoryName;
    private String containerRegistryRedisUrl;
    private String containerRegistryRedisPassword;
    private String containerImageName;
    private String buildTool; // buildToolType is from settings which build tool this setting is for, whereas buildTool is selected in create application.
    private String buildToolType;
    private String buildToolSettingsData;
    private String baseBuildPath;
    private String buildToolSettingId;
    private String deploymentPipelineType;
    private String devKubernetesClusterSettingId;
    private String devKubernetesBaseAddress;
    private String devKubeconfig;
    private String devKubernetesNamespace;
    private String prodKubernetesClusterSettingId;
    private String prodKubeconfig;
    private String prodKubernetesNamespace;
    private String gunicornAppLocation;
    private String dotnetcoreProjectLocation;
}
