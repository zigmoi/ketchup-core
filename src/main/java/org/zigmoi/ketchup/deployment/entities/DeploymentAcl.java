package org.zigmoi.ketchup.deployment.entities;


import lombok.Data;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class DeploymentAcl {
    @Id
    private String aclRuleId;
    private String permissionId;
    private String identity;
    private DeploymentId deploymentId;
}
