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
import java.util.*;

@Data
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "application_revisions")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class Revision {

    @NotNull
    @EmbeddedId
    private RevisionId id;

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
    private String version;

    @Pattern(regexp = "IN PROGRESS|SUCCESS|FAILED")
    private String status;

    @Size(max = 65535)
    @Column(columnDefinition="TEXT")
    private String errorMessage;

    @Size(max = 65535)
    @Column(columnDefinition="TEXT")
    private String pipelineStatusJson;

    @Size(max = 100)
    private String commitId;

//    @NotBlank
    private String helmChartId; // this will be part of deployment but copied here so we know which chart was used.

    @Size(max = 100)
    private String helmReleaseId;

    @NotBlank
    @Size(max = 65535)
    @Column(columnDefinition="TEXT")
    private String applicationDataJson;

}
