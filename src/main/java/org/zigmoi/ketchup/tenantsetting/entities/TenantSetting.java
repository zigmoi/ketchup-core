package org.zigmoi.ketchup.tenantsetting.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class TenantSetting {
    @EmbeddedId
    private TenantSettingId id;

//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "global_setting_members",
//            joinColumns = {
//                    @JoinColumn(name = "globalSettingTenantId", referencedColumnName = "globalSettingTenantId"),
//                    @JoinColumn(name = "globalSettingResourceId", referencedColumnName = "globalSettingResourceId")
//            })
//    @Column(name = "member")
//    Set<String> members = new HashSet<>();
}
