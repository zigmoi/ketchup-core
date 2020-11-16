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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "projects")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class Project {
    @NotNull
    @EmbeddedId
    private ProjectId id;

    @Size(max=100)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Date createdOn;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date lastUpdatedOn;

    @LastModifiedBy
    private String lastUpdatedBy;

}

