package org.zigmoi.ketchup.iam.authz.entities;


import lombok.Data;
import org.zigmoi.ketchup.project.entities.ProjectSettingId;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class ProjectSettingAclStore {
    @Id
    private String aclRuleId;
    private String permissionId;
    private String identity;
    private ProjectSettingId projectSettingId;
}
