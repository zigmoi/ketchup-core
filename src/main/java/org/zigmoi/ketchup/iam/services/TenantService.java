package org.zigmoi.ketchup.iam.services;

import org.zigmoi.ketchup.iam.dtos.TenantDto;
import org.zigmoi.ketchup.iam.entities.Tenant;

import java.util.List;
import java.util.Optional;

public interface TenantService {

    void createTenant(TenantDto tenantDto);

    void deleteTenant(String tenantId);

    void updateTenantStatus(String tenantId, boolean status);

    void updateTenantDisplayName(String tenantId, String displayName);

    void updateMyTenantDisplayName(String displayName);

    //unprotected for service calls to works (loadUserByUsername in UserServiceImpl)
    //but any controller if un protected will allow access to any tenants information.
    Optional<Tenant> getTenant(String tenantId);

    Tenant getMyTenantDetails();

    List<Tenant> listAllTenants();
}
