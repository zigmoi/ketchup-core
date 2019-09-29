package org.zigmoi.ketchup.project.dtos;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.CorePermissionMeta;

@Data
public class ProjectPermissionStatusDto {
    private CorePermissionMeta permission;
    private boolean status;
}
