package org.zigmoi.ketchup.deployment.entities;

import lombok.Data;

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
    private String effect;
}
