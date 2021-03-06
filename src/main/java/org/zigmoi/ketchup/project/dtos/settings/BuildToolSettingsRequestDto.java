package org.zigmoi.ketchup.project.dtos.settings;

import lombok.Data;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidProjectId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class BuildToolSettingsRequestDto {

    @ValidDisplayName
    private String displayName;


    @NotBlank
    @Pattern(regexp = "maven-3.3|gradle-5.5")
    private String type;

    @NotBlank
    @Size(max = 65535)
    private String fileData;
}
