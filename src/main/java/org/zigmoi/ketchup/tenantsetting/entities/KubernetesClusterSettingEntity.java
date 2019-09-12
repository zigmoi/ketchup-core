package org.zigmoi.ketchup.tenantsetting.entities;

import lombok.Data;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@Entity
@Table(name = "tenant_setting_kubernetes_cluster")
public class KubernetesClusterSettingEntity extends TenantEntity {

    @Id
    @NotBlank(message = "Please provide git provider id.")
    private String id;
    private String repoListUrl;
    private String displayName;
    private String provider;
    private String username, password;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    private String createdBy;
}
