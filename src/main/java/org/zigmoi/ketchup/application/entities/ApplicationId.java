package org.zigmoi.ketchup.application.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ApplicationId extends TenantEntity implements Serializable  {

    @Column(length = 36)
    private String projectResourceId;
    @Column(length = 36)
    private String applicationResourceId;

    public ApplicationId(){}

    public ApplicationId(String tenantId, String projectResourceId, String applicationResourceId) {
        super.tenantId = tenantId;
        this.projectResourceId = projectResourceId;
        this.applicationResourceId = applicationResourceId;
    }
}
