package org.zigmoi.ketchup.tenantsetting.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@Entity
@Table(name = "tenant_cloud_registry")
public class ContainerRegistrySettingEntity extends TenantEntity {
    @Id
    @NotBlank(message = "Please provide build tool id.")
    private String id;
    private String provider;
    private String displayName;
    private String cloudCredentialId;
    private String registryId, registryUrl;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    private String createdBy;
}
