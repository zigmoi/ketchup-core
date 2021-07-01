package org.zigmoi.ketchup.application.dtos;

import lombok.Data;
import org.zigmoi.ketchup.application.entities.RevisionId;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class RevisionBasicResponseDto {

    private RevisionId id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedOn;

    private String lastUpdatedBy;

    private String version;

    @Pattern(regexp = "IN PROGRESS|SUCCESS|FAILED")
    private String status;

    @Pattern(regexp = "GIT WEBHOOK|MANUAL")
    private String deploymentTriggerType;

    private String commitId;

    private String helmChartId; // this will be part of deployment but copied here so we know which chart was used.
    private String helmReleaseId;
    private String helmReleaseVersion;

    private boolean rollback;

    private String originalRevisionVersionId;

    /* only if full is true */
    private String errorMessage;
    private String pipelineStatusJson;
    private String applicationDataJson;
}
