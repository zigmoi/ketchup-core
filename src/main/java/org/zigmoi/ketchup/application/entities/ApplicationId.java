package org.zigmoi.ketchup.application.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.common.validations.ValidResourceId;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ApplicationId extends TenantEntity implements Serializable  {

    @ValidProjectId
    @Column(length = 36)
    private String projectResourceId;

    @ValidResourceId
    @Column(length = 36)
    private String applicationResourceId;

    public ApplicationId(){}

    public ApplicationId(String tenantId, String projectResourceId, String applicationResourceId) {
        super.tenantId = tenantId;
        this.projectResourceId = projectResourceId;
        this.applicationResourceId = applicationResourceId;
    }
}
