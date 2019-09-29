package org.zigmoi.ketchup.iam.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @Column(length = 50)
    private String id;
    private String displayName;
    private boolean enabled;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
}