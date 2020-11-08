package org.zigmoi.ketchup.project.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.common.validations.ValidProjectId;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ProjectId extends TenantEntity implements Serializable {

    @ValidProjectId
    @Column(length = 36)
    private String resourceId;
}
