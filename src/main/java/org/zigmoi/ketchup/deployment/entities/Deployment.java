package org.zigmoi.ketchup.deployment.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class Deployment {
    @EmbeddedId
    private DeploymentId id;

//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "deployment_members",
//            joinColumns = {
//                    @JoinColumn(name = "deploymentTenantId", referencedColumnName = "deploymentTenantId"),
//                    @JoinColumn(name = "deploymentProjectId", referencedColumnName = "deploymentProjectId"),
//                    @JoinColumn(name = "deploymentResourceId", referencedColumnName = "deploymentResourceId")
//            })
//    @Column(name = "member")
//    Set<String> members = new HashSet<>();
}
