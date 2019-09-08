package org.zigmoi.ketchup.iam.authz.entities;

import lombok.Data;
import org.zigmoi.ketchup.project.entities.ProjectId;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class ProjectAcl {
    @Id
    private String aclRuleId;
    private String permissionId;
    private String identity;
    private ProjectId projectId;
    private String effect;
}
