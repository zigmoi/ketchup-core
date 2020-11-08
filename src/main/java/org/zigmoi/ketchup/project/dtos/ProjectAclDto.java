package org.zigmoi.ketchup.project.dtos;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.Set;

@Data
public class ProjectAclDto {
    @NotBlank
    @Size(max = 100)
    private String identity;

    @NotBlank
    @Size(max = 20)
    private String projectResourceId;

    @NotNull
    @NotEmpty
    private Set<
            @NotBlank
            @Pattern(regexp = "create-project|read-project|update-project|delete-project|" +
            "assign-create-project|assign-read-project|assign-update-project|assign-delete-project")
            String> permissions;
}
