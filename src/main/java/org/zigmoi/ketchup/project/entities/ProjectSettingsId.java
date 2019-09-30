package org.zigmoi.ketchup.project.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ProjectSettingsId extends TenantEntity implements Serializable {
    private String projectId;
    private String settingId;
}
