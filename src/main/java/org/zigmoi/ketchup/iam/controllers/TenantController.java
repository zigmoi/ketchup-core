package org.zigmoi.ketchup.iam.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.dtos.TenantDto;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.repositories.TenantRepository;
import org.zigmoi.ketchup.iam.services.TenantService;

import java.util.Date;
import java.util.List;

@RestController
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @PostMapping("/v1/tenant")
    public void createTenant(@RequestBody TenantDto tenantDto) {
        tenantService.createTenant(tenantDto);
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @PutMapping("/v1/tenant/{id}/enable/{status}")
    public void updateTenantStatus(@PathVariable("id") String tenantId, @PathVariable("status") boolean status) {
        tenantService.updateTenantStatus(tenantId, status);
    }

    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/v1/tenant/{id}/displayName/{displayName}")
    public void updateTenantDisplayName(@PathVariable("id") String tenantId, @PathVariable("displayName") String displayName) {
        tenantService.updateTenantDisplayName(tenantId, displayName);
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @GetMapping("/v1/tenant/{id}")
    public Tenant getTenant(@PathVariable("id") String tenantId) {
        Tenant tenant = tenantService.getTenant(tenantId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        return tenant;
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @DeleteMapping("/v1/tenant/{id}")
    public void deleteTenant(@PathVariable("id") String tenantId) {
        tenantService.deleteTenant(tenantId);
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @GetMapping("/v1/tenants")
    public List<Tenant> listAllTenants() {
        return tenantService.listAllTenants();
    }
}
