package org.zigmoi.ketchup.application.dtos;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidResourceId;

import javax.validation.constraints.*;

@Data
public class ApplicationRequestDto {
    @ValidDisplayName
    private String displayName;

    @Size(max = 100)
    private String description;

    @NotNull
    @Pattern(regexp = "WEB-APPLICATION")
    private String applicationType;

    @NotBlank
    @Size(max = 100)
    private String serviceName;

    @NotBlank
    @Pattern(regexp = "ClusterIP|NodePort")
    private String serviceType;

    @NotBlank
    @Range(min = 1, max = 65535)
    private String appServerPort;

    @NotBlank
    @Range(min = 1, max = 99)
    private String replicas;

    @NotNull
    @Pattern(regexp = "recreate")
    private String deploymentStrategy;

    @NotBlank
    @URL
    @Size(max = 250)
    private String gitRepoUrl;

    @NotBlank
    @Size(max = 100)
    private String gitRepoUsername;

    @NotBlank
    @Size(max = 100)
    private String gitRepoPassword;

    @NotBlank
    @Size(max = 100)
    private String gitRepoBranchName;

    @NotBlank
    @Size(max = 10)
    private String continuousDeployment;

    @Range(min = 1, max = 604800) //no of seconds in a week.
    private String gitRepoPollingInterval;

    @NotNull
    @Pattern(regexp = "java-8|java-11|python-3.8")
    private String platform;

    @ValidResourceId
    private String containerRegistrySettingId;

    @NotBlank
    @Size(min=1, max= 100)
    private String containerImageName;

    @NotNull
    @Pattern(regexp = "maven-3.3|pip-3")
    private String buildTool;

    @NotBlank
    @Size(max = 250)
    private String baseBuildPath;

//    @Size(min = 36, max = 36) //can be null or blank as its optional.
    private String buildToolSettingId;

    @NotNull
    @Pattern(regexp = "standard-dev-1.0")
    private String deploymentPipelineType;

    @ValidResourceId
    private String devKubernetesClusterSettingId;

    @NotBlank
    @Size(max=100)
    private String devKubernetesNamespace;

//    @Size(min = 36, max = 36) //can be null or blank as its optional.
    private String prodKubernetesClusterSettingId;

    @Size(max = 100) //can be null or blank as its optional.
    private String prodKubernetesNamespace;
}
