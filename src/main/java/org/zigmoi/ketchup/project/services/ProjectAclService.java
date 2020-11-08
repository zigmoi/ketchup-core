package org.zigmoi.ketchup.project.services;

import org.springframework.validation.annotation.Validated;
import org.zigmoi.ketchup.project.dtos.ProjectAclDto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Validated
public interface ProjectAclService {

     void assignPermission(@Valid ProjectAclDto projectAclDto);
     void revokePermission(@Valid ProjectAclDto projectAclDto);
     boolean hasProjectPermission(@NotBlank @Size(max = 100) String identity,
                                  @NotBlank @Pattern(regexp = "create-project|" +
                                          "read-project|update-project|delete-project|" +
                                          "assign-create-project|assign-read-project|" +
                                          "assign-update-project|assign-delete-project") String permission,
                                  @NotBlank @Size(max = 20) String projectResourceId);
}
