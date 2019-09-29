package org.zigmoi.ketchup.project.entities;

import lombok.Data;
import org.hibernate.annotations.Filter;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.project.entities.ProjectId;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

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
