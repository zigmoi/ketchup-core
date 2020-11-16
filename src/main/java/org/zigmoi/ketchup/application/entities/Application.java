package org.zigmoi.ketchup.application.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "applications")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class Application {
    @NotNull
    @EmbeddedId
    private ApplicationId id;

    @NotNull
    @Pattern(regexp = "WEB-APPLICATION")
    private String type;

    @ValidDisplayName
    private String displayName;

    @NotBlank
    @Size(max = 65535)
    @Column(columnDefinition="TEXT")
    private String data;

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


    public String getProjectResourceId(){
        return this.id.getProjectResourceId();
    }

    public String getApplicationResourceId(){
        return this.id.getApplicationResourceId();
    }
}
