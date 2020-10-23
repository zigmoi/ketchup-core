package org.zigmoi.ketchup.application.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class PipelineArtifactId extends TenantEntity implements Serializable  {

    @Column(length = 36)
    private String projectResourceId;
    @Column(length = 36)
    private String applicationResourceId;
    @Column(length = 36)
    private String revisionResourceId;
    @Column(length = 36)
    private String pipelineArtifactResourceId;

    public PipelineArtifactId(){}

    public PipelineArtifactId(String tenantId, String projectResourceId, String applicationResourceId, String revisionResourceId, String pipelineArtifactResourceId) {
        super.tenantId = tenantId;
        this.projectResourceId = projectResourceId;
        this.applicationResourceId = applicationResourceId;
        this.revisionResourceId = revisionResourceId;
        this.pipelineArtifactResourceId = pipelineArtifactResourceId;
    }

    public PipelineArtifactId(RevisionId revisionId, String pipelineArtifactResourceId) {
        super.tenantId = revisionId.getTenantId();
        this.projectResourceId = revisionId.getProjectResourceId();
        this.applicationResourceId = revisionId.getApplicationResourceId();
        this.revisionResourceId = revisionId.getRevisionResourceId();
        this.pipelineArtifactResourceId = pipelineArtifactResourceId;
    }
}
