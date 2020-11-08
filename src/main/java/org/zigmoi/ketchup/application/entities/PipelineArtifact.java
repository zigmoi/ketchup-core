package org.zigmoi.ketchup.application.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "pipeline_artifacts")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class PipelineArtifact {

    @NotNull
    @EmbeddedId
    private PipelineArtifactId id;

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

    @NotBlank
    @Size(max = 65535)
    @Column(columnDefinition="TEXT")
    private String resourceContent;

    @NotNull
    @Pattern(regexp = "json|yaml")
    private String format;

    @NotNull
    @Pattern(regexp = "configmap|secret|service-account|task|pipeline|pipeline-run|pipeline-pvc")
    private String resourceType;


}
