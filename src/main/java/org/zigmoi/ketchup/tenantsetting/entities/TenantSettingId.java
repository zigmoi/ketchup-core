package org.zigmoi.ketchup.tenantsetting.entities;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class TenantSettingId implements Serializable {
    private String tenantId;
    private String resourceId;
}
