package org.zigmoi.ketchup.project.dtos;

import lombok.Data;
import org.zigmoi.ketchup.common.validations.ValidProjectId;

import javax.validation.constraints.Size;

@Data
public class ProjectDto {
    @ValidProjectId
    private String projectResourceId;
    @Size(max=100)
    private String description;
}

