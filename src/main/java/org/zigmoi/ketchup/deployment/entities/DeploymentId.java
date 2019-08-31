package org.zigmoi.ketchup.deployment.entities;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class DeploymentId implements Serializable {
    private String deploymentTenantId;
    private String deploymentProjectId;
    private String deploymentResourceId;
}
