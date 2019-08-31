package org.zigmoi.ketchup.project.entities;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ProjectSettingId implements Serializable {
    private String projectSettingTenantId;
    private String ProjectSettingProjectId;
    private String projectSettingResourceId;
}
