package org.zigmoi.ketchup.iam.entities;

import lombok.Data;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@FilterDef(
        name = TenantEntity.TENANT_FILTER_NAME,
        parameters = @ParamDef(name = TenantEntity.TENANT_FILTER_ARGUMENT_NAME, type = "string"),
        defaultCondition = TenantEntity.TENANT_ID_PROPERTY_NAME + " = :" + TenantEntity.TENANT_FILTER_ARGUMENT_NAME)
public class TenantEntity {
    public static final String TENANT_FILTER_NAME = "tenantFilter";
    //This is NOT Entity attribute name, this is name generated in query or table column name.
    public static final String TENANT_ID_PROPERTY_NAME = "tenant_id";
    public static final String TENANT_FILTER_ARGUMENT_NAME = "tenantId";

    @Column(length = 50)
    protected String tenantId;
}
