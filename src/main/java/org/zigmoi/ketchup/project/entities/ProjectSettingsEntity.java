package org.zigmoi.ketchup.project.entities;

import lombok.Data;
import org.hibernate.annotations.Filter;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "project_settings")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class ProjectSettingsEntity {
    @EmbeddedId
    private ProjectSettingsId id;
    private String type;
    private String displayName;
    private String data;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    private String createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
}
