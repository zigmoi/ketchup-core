package org.zigmoi.ketchup.iam.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class CorePermissionMeta {
    @Id
    private String permissionId;
    private String permissionCategory;
    private String permissionDescription;
}
