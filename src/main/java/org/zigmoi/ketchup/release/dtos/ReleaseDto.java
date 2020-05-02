package org.zigmoi.ketchup.release.dtos;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.release.entities.ReleaseId;

import javax.persistence.*;
import java.util.Date;

@Data
public class ReleaseDto {

    private ReleaseId id;
    private String statusJson;
    private String commitId;
    private String helmChartId;
    private String helmValuesJson;
    private String pipelineTemplateId;
    private String pipelineArtificatsId;
}
