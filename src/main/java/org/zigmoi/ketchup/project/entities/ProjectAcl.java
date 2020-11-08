package org.zigmoi.ketchup.project.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Filter;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import lombok.Data;

@Data
@Entity
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class ProjectAcl {
    @NotNull
    @EmbeddedId
    private ProjectAclId projectAclId;

    @NotBlank
    @Pattern(regexp = "create-project|read-project|update-project|delete-project|" +
            "assign-create-project|assign-read-project|assign-update-project|assign-delete-project")
    private String permissionId;

    @NotBlank
    @Size(max = 100)
    private String identity;

    @NotBlank
    @Size(max = 20)
    private String projectResourceId;

    @NotNull
    @Pattern(regexp = "ALLOW|DENY")
    private String effect;
}
