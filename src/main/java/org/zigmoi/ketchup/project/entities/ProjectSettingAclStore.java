package org.zigmoi.ketchup.project.entities;


import lombok.Data;

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
