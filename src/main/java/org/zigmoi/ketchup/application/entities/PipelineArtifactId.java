package org.zigmoi.ketchup.application.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.common.validations.ValidResourceId;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class PipelineArtifactId extends TenantEntity implements Serializable  {

    @ValidProjectId
    @Column(length = 36)
    private String projectResourceId;

    @ValidResourceId
    @Column(length = 36)
    private String applicationResourceId;

    @ValidResourceId
    @Column(length = 36)
    private String revisionResourceId;

    @ValidResourceId
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
