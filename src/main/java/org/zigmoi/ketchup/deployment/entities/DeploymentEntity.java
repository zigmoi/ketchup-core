package org.zigmoi.ketchup.deployment.entities;

import lombok.Data;
import org.hibernate.annotations.Filter;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "deployments")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class DeploymentEntity {
    @EmbeddedId
    private DeploymentId id;
    private String type;
    private String displayName;
    private String serviceName;
    private String data;
    private String currentStatus;
    private String previousStatus;
    private String errorMessage;
    private String logs;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    private String createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
}
