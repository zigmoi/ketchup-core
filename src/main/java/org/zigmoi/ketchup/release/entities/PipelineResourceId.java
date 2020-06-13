package org.zigmoi.ketchup.release.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class PipelineResourceId extends TenantEntity implements Serializable  {

    @Column(length = 36)
    private String guid;

    public PipelineResourceId(){}

    public PipelineResourceId(String tenantId, String guid) {
        super.tenantId = tenantId;
        this.guid = guid;
    }
}
