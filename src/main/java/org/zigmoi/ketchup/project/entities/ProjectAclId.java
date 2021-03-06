package org.zigmoi.ketchup.project.entities;

import lombok.Data;
import org.zigmoi.ketchup.common.validations.ValidResourceId;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ProjectAclId extends TenantEntity implements Serializable {

    @ValidResourceId
    @Column(length = 36)
    private String aclRuleId;

}
