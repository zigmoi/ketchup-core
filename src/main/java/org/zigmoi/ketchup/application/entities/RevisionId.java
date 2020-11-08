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
public class RevisionId extends TenantEntity implements Serializable  {

    @ValidProjectId
    @Column(length = 36)
    private String projectResourceId;

    @ValidResourceId
    @Column(length = 36)
    private String applicationResourceId;

    @ValidResourceId
    @Column(length = 36)
    private String revisionResourceId;

    public RevisionId(){}

    public RevisionId(String tenantId, String projectResourceId, String applicationResourceId, String revisionResourceId) {
        super.tenantId = tenantId;
        this.projectResourceId = projectResourceId;
        this.applicationResourceId = applicationResourceId;
        this.revisionResourceId = revisionResourceId;
    }

    public RevisionId(ApplicationId applicationId, String revisionResourceId){
        super.tenantId = applicationId.getTenantId();
        this.projectResourceId = applicationId.getProjectResourceId();
        this.applicationResourceId = applicationId.getApplicationResourceId();
        this.revisionResourceId = revisionResourceId;
    }
}
