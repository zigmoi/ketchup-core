package org.zigmoi.ketchup.application.dtos;

import lombok.Data;
import org.zigmoi.ketchup.application.entities.RevisionId;

@Data
public class RevisionDto {

    private RevisionId id;
    private String version;
    private String statusJson;
    private String commitId;
    private String helmChartId;
    private String helmValuesJson;
    private String pipelineTemplateId;
    private String pipelineArtificatsId;
}
