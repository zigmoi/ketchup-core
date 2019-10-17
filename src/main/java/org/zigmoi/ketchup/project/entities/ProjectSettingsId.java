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
public class ProjectSettingsId extends TenantEntity implements Serializable {
    @Column(length = 36)
    private String projectId;
    @Column(length = 36)
    private String settingId;

    public ProjectSettingsId(String tenantId, String projectId, String settingId) {
        super.tenantId = tenantId;
        this.projectId = projectId;
        this.settingId = settingId;
    }
}
