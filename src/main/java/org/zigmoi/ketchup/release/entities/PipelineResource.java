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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "pipeline_resources")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class PipelineResource {

    @EmbeddedId
    private PipelineResourceId id;

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

    private String projectResourceId;
    private String releaseResourceId;

    private String resourceContent;
    private String format; //json|yaml
    private String resourceType; //secret|service-account|task|pipeline|pipeline-run|pipeline-resource


}