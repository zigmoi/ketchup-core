package org.zigmoi.ketchup.tenantsetting.entities;

import lombok.Data;
import org.zigmoi.ketchup.tenantsetting.entities.TenantSettingId;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class TenantSettingAclStore {
    @Id
    private String aclRuleId;
    private String permissionId;
    private String identity;
    private TenantSettingId tenantSettingId;
}
