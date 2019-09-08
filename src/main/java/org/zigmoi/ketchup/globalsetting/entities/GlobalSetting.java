package org.zigmoi.ketchup.globalsetting.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class GlobalSetting {
    @EmbeddedId
    private GlobalSettingId id;

//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "global_setting_members",
//            joinColumns = {
//                    @JoinColumn(name = "globalSettingTenantId", referencedColumnName = "globalSettingTenantId"),
//                    @JoinColumn(name = "globalSettingResourceId", referencedColumnName = "globalSettingResourceId")
//            })
//    @Column(name = "member")
//    Set<String> members = new HashSet<>();
}
