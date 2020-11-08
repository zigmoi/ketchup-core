package org.zigmoi.ketchup.project.dtos;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ProjectUpdateDto {
    @Size(max=100)
    private String description;
}

