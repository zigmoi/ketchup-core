package org.zigmoi.ketchup.deployment.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class DeploymentId extends TenantEntity implements Serializable  {

    private String projectResourceId;
    private String deploymentResourceId;

    public DeploymentId(){}

    public DeploymentId(String tenantId, String projectResourceId, String deploymentResourceId) {
        super.tenantId = tenantId;
        this.projectResourceId = projectResourceId;
        this.deploymentResourceId = deploymentResourceId;
    }
}
