package org.zigmoi.ketchup.deployment.dtos;

import com.sun.istack.Nullable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel
public class BasicSpringBootDeploymentRequestDto {

    @NotNull @NotEmpty
    private String displayName;

    @ApiModelProperty(notes = "Must be unique. Must match regex \"^[a-zA-Z0-9_-]+$\"")
    @Pattern(regexp="^[a-zA-Z0-9_-]+$")
    @NotNull @NotEmpty
    private String serviceName;

    @NotNull @NotEmpty
    private String gitProviderSettingId;
    @NotNull @NotEmpty
    private String buildToolSettingId;
    @NotNull @NotEmpty
    private String cloudProviderSettingId;
    @NotNull @NotEmpty
    private String containerRegistrySettingId;
    @NotNull @NotEmpty
    private String kubernetesClusterSettingId;
    @ApiModelProperty(notes = "Optional. If blank, make sure that all of the external resources are referred using IP, in the code.")
    private String externalResourceIpHostnameMappingSettingId;

    @NotNull @NotEmpty
    private String kubernetesNamespace;

    @NotNull @NotEmpty
    @ApiModelProperty(notes = "Mandatory. If repo name is nested provide full path except base provider URL." +
            " Example: If repo URL is https://gitlab.com/zigmoi/ketchup/ketchup-demo-basicspringboot uses latest commit," +
            " gitRepoName should be zigmoi/ketchup/ketchup-demo-basicspringboot")
    private String gitRepoName;
    @ApiModelProperty(notes = "Optional. If blank, uses latest commit.")
    @Nullable
    private String gitRepoCommitId;
    @ApiModelProperty(notes = "Optional. If blank, uses master branch.")
    @Nullable
    private String gitRepoBranchName;
    @ApiModelProperty(notes = "Optional. If blank, runs build on the base path of the repo.")
    @Nullable
    private String gitRepoToBuildDirectory = ".";

    @ApiModelProperty(notes = "Optional. If blank, uses the service_${service_Name} nomenclature. Throws exception if image already exists in the container registry.")
    @Nullable
    private String dockerImageName = "service_" + getServiceName();

    @ApiModelProperty(notes = "Optional. If blank, set tag value 'latest'")
    @Nullable
    private String dockerImageTag = "latest";

    @ApiModelProperty(notes = "Mandatory. If using AWS ECR, use the repo name without registry url. Ex: zigmoi/ketchup-ui")
    @Nullable
    private String dockerImageRepoName;

    @ApiModelProperty(notes = "Optional. If blank, uses port 8080")
    @Nullable
    private String appServerPort = "8080";
    @ApiModelProperty(notes = "Optional. If blank, uses UTC timezone")
    @Nullable
    private String appTimezone = "UTC";

    @ApiModelProperty(notes = "Optional. If blank, uses /app. Make sure that the code does not refer any file based on app home or base path.")
    @Nullable
    private String appBasePath = "/app";
}
