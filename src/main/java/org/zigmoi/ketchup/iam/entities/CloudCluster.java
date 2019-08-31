package org.zigmoi.ketchup.iam.entities;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@Entity
@Table(name = "tenant_cloud_cluster")
public class CloudCluster extends TenantEntity {

    @Id
    @NotBlank(message = "Please provide build tool id.")
    private String id;
    private String provider;
    private String displayName;
    private String fileName, fileRemoteUrl;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    private String createdBy;
}
