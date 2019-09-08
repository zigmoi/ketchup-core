package org.zigmoi.ketchup.project.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ProjectId extends TenantEntity implements Serializable {

    @Column(length = 36)
    private String resourceId;

}
