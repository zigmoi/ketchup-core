package org.zigmoi.ketchup.project.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class SettingId extends TenantEntity implements Serializable {
    @Column(length = 36)
    private String projectResourceId;
    @Column(length = 36)
    private String settingResourceId;

    public SettingId(String tenantId, String projectResourceId, String settingResourceId) {
        super.tenantId = tenantId;
        this.projectResourceId = projectResourceId;
        this.settingResourceId = settingResourceId;
    }
}
