package org.zigmoi.ketchup.release.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "deployment_releases")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class Release {

    @EmbeddedId
    private ReleaseId id;

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

    private String statusJson;
    private String commitId;
    private String helmChartId; // this will be part of deployment but copied here so we know which chart was used.
    private String helmValuesJson; // this will be part of deployment but copied here so we know what values were used.
    private String pipelineTemplateId; // this will be part of deployment but copied here so we know which template was used.
    private String pipelineArtificatsId; // this will be part of deployment but copied here so we know which template was used.
    private String pipelineRunId;
    private String pipelineId;
    private String pipelineResourceIds; //comma separated values.
    private String pipelineSecretIds; //comma separated values.
    private String pipelineServiceAccountIds; //comma separated values.
    private String pipelineTaskIds; //comma separated values.
    private String helmReleaseId;

}
