package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidProjectId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class ContainerRegistrySettingsRequestDto {

    @ValidProjectId
    private String projectResourceId;

    @ValidDisplayName
    private String displayName;

    @NotBlank
    @Pattern(regexp = "container-registry")
    private String type;

    @NotBlank
    @Size(max=250)
    private String registryUrl;

    @Size(max=100)
    private String repository; //project id for gcr

    @Size(max=100)
    private String registryUsername;

    @Size(max=5000)
    private String registryPassword;
}
