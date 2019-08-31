package org.zigmoi.ketchup.project.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "projects")
public class Project {
    @EmbeddedId
    private ProjectId id;

    private String description;

//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "project_members",
//            joinColumns = {
//                    @JoinColumn(name = "tenantId", referencedColumnName = "tenantId"),
//                    @JoinColumn(name = "resourceId", referencedColumnName = "resourceId")
//            })
//    @Column(name = "member")
//    Set<String> members = new HashSet<>();
}

