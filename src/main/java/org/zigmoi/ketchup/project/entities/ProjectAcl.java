package org.zigmoi.ketchup.project.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import org.hibernate.annotations.Filter;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import lombok.Data;

@Data
@Entity
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class ProjectAcl {
    @EmbeddedId
    private ProjectAclId projectAclId;
    private String permissionId;
    private String identity;
    private String projectResourceId;
    private String effect;
}
