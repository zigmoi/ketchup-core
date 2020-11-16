package org.zigmoi.ketchup.iam.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidTenantId;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tenants")
public class Tenant {

    @ValidTenantId
    @Id
    @Column(length = 50)
    private String id;

    @ValidDisplayName
    private String displayName;

    @NotNull
    private boolean enabled;

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