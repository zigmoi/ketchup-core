package org.zigmoi.ketchup.iam.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.dtos.TenantDto;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.services.TenantService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1-alpha/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping
    public void createTenant(@RequestBody TenantDto tenantDto) {
        tenantService.createTenant(tenantDto);
    }

    @PutMapping("/{id}/enable/{status}")
    public void updateTenantStatus(@PathVariable("id") String tenantId, @PathVariable("status") boolean status) {
        tenantService.updateTenantStatus(tenantId, status);
    }

    @PutMapping("/{id}/displayName/{displayName}")
    public void updateTenantDisplayName(@PathVariable("id") String tenantId, @PathVariable("displayName") String displayName) {
        tenantService.updateTenantDisplayName(tenantId, displayName);
    }

    @PutMapping("/my/displayName/{displayName}")
    public void updateMyTenantDisplayName(@PathVariable("displayName") String displayName) {
        tenantService.updateMyTenantDisplayName(displayName);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant getTenant(@PathVariable("id") String tenantId) {
        Tenant tenant = tenantService.getTenant(tenantId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        return tenant;
    }

    @GetMapping("/my/profile")
    public Tenant getTenant() {
        return tenantService.getMyTenantDetails();
    }

    @DeleteMapping("/{id}")
    public void deleteTenant(@PathVariable("id") String tenantId) {
        tenantService.deleteTenant(tenantId);
    }

    @GetMapping
    public List<Tenant> listAllTenants() {
        return tenantService.listAllTenants()
                .stream()
                .sorted(Comparator.comparing(Tenant::getId))
                .collect(Collectors.toList());
    }
}
