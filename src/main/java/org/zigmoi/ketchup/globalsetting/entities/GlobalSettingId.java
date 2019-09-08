package org.zigmoi.ketchup.globalsetting.entities;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class GlobalSettingId implements Serializable {
    private String globalSettingTenantId;
    private String globalSettingResourceId;
}
