package org.zigmoi.ketchup.release.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class DeploymentId extends TenantEntity implements Serializable  {

    @Column(length = 36)
    private String projectResourceId;
    @Column(length = 36)
    private String deploymentResourceId;

    public DeploymentId(){}

    public DeploymentId(String tenantId, String projectResourceId, String deploymentResourceId) {
        super.tenantId = tenantId;
        this.projectResourceId = projectResourceId;
        this.deploymentResourceId = deploymentResourceId;
    }
}
