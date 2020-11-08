package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidProjectId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class KubernetesClusterSettingsRequestDto {

    @ValidProjectId
    private String projectResourceId;

    @ValidDisplayName
    private String displayName;

    @NotBlank
    @Size(max = 65535)
    private String kubeconfig;
}
