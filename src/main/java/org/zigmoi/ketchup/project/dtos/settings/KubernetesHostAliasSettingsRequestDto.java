package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidProjectId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

@Data
public class KubernetesHostAliasSettingsRequestDto {

    @ValidDisplayName
    private String displayName;

    @NotNull
    @NotEmpty
    private Map<@NotBlank @Size(max = 250) String, @NotBlank @Size(max = 250) String> hostnameIpMapping;
}
