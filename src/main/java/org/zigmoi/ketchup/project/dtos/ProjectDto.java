package org.zigmoi.ketchup.project.dtos;

import lombok.Data;
import org.hibernate.annotations.Filter;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.project.entities.ProjectId;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
public class ProjectDto {
    private String projectResourceId;
    private String description;
    Set<String> members = new HashSet<>();
}

