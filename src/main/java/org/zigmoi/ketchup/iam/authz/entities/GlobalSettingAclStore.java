package org.zigmoi.ketchup.iam.authz.entities;


import lombok.Data;
import org.zigmoi.ketchup.globalsetting.entities.GlobalSettingId;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class GlobalSettingAclStore {
    @Id
    private String aclRuleId;
    private String permissionId;
    private String identity;
    private GlobalSettingId globalSettingId;
}
