package org.zigmoi.ketchup.release.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ReleaseId extends TenantEntity implements Serializable  {

    @Column(length = 36)
    private String releaseResourceId;

    public ReleaseId(){}

    public ReleaseId(String tenantId, String releaseResourceId) {
        super.tenantId = tenantId;
        this.releaseResourceId = releaseResourceId;
    }
}
