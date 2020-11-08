package org.zigmoi.ketchup.project.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.iam.entities.TenantEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "project_settings")
@Filter(name = TenantEntity.TENANT_FILTER_NAME)
public class Setting {

    @NotNull
    @EmbeddedId
    private SettingId id;

    @NotNull
    @Pattern(regexp = "build-tool|container-registry|kubernetes-cluster|k8s-host-alias")
    private String type;

    @ValidDisplayName
    private String displayName;

    @NotBlank
    @Size(max = 65535)
    @Column(columnDefinition="TEXT")
    private String data;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdOn;

    @CreatedBy
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date lastUpdatedOn;

    @LastModifiedBy
    private String lastUpdatedBy;

    public String getProjectResourceId(){
       return this.getId().getProjectResourceId();
    }

    public String getSettingResourceId(){
        return this.getId().getSettingResourceId();
    }
}
