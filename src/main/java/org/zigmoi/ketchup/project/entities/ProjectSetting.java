package org.zigmoi.ketchup.project.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class ProjectSetting {
    @EmbeddedId
    private ProjectSettingId id;

//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "project_setting_members",
//            joinColumns = {
//                    @JoinColumn(name = "projectSettingTenantId", referencedColumnName = "projectSettingTenantId"),
//                    @JoinColumn(name = "projectSettingProjectId", referencedColumnName = "projectSettingProjectId"),
//                    @JoinColumn(name = "projectSettingResourceId", referencedColumnName = "projectSettingResourceId")
//            })
//    @Column(name = "member")
//    Set<String> members = new HashSet<>();
}
