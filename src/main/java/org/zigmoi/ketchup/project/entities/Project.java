package org.zigmoi.ketchup.project.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "projects")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class Project {
    @EmbeddedId
    private ProjectId id;

    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdOn;

    @CreatedBy
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date lastUpdatedOn;

    @LastModifiedBy
    private String lastUpdatedBy;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_members",
            joinColumns = {
                    @JoinColumn(name = "tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "resourceId", referencedColumnName = "resourceId")
            })
    @Column(name = "member")
    Set<String> members = new HashSet<>();

}

