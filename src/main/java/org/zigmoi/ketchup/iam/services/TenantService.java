package org.zigmoi.ketchup.iam.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.zigmoi.ketchup.iam.dtos.TenantDto;
import org.zigmoi.ketchup.iam.entities.Tenant;

import java.util.List;
import java.util.Optional;

//only ROLE_SUPER_ADMIN can perform tenant related operations.
public interface TenantService {

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    void createTenant(TenantDto tenantDto);

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    void deleteTenant(String tenantId);

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    void updateTenantStatus(String tenantId, boolean status);

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TENANT_ADMIN')")
    void updateTenantDisplayName(String tenantId, String displayName);

    //unprotected for service calls to works (loadUserByUsername in UserServiceImpl)
    //but any controller if un protected will allow access to any tenants information.
    Optional<Tenant> getTenant(String tenantId);

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    List<Tenant> listAllTenants();
}
