package org.zigmoi.ketchup.iam.services;

import org.springframework.validation.annotation.Validated;
import org.zigmoi.ketchup.iam.dtos.TenantDto;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidTenantId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Validated
public interface TenantService {

    void createTenant(@Valid TenantDto tenantDto);

    void deleteTenant(@ValidTenantId String tenantId);

    void updateTenantStatus(@ValidTenantId String tenantId, @NotNull boolean status);

    void updateTenantDisplayName(@ValidTenantId String tenantId, @ValidDisplayName String displayName);

    void updateMyTenantDisplayName(@ValidDisplayName String displayName);

    //unprotected for service calls to works (loadUserByUsername in UserServiceImpl)
    //but any controller if un protected will allow access to any tenants information.
    Optional<Tenant> getTenant(@ValidTenantId String tenantId);

    Tenant getMyTenantDetails();

    List<Tenant> listAllTenants();
}
