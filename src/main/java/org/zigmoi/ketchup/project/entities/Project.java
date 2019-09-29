package org.zigmoi.ketchup.project.entities;

import lombok.Data;
import org.hibernate.annotations.Filter;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Table(name = "projects")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class Project {
    @EmbeddedId
    private ProjectId id;

    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_members",
            joinColumns = {
                    @JoinColumn(name = "tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "resourceId", referencedColumnName = "resourceId")
            })
    @Column(name = "member")
    Set<String> members = new HashSet<>();

}

