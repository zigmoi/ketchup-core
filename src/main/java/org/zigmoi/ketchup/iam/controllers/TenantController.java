package org.zigmoi.ketchup.iam.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.dtos.TenantDto;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.services.TenantService;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;
import org.zigmoi.ketchup.common.validations.ValidTenantId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/v1-alpha/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping
    public void createTenant(@Valid @RequestBody TenantDto tenantDto) {
        tenantService.createTenant(tenantDto);
    }

    @PutMapping("/{id}/enable/{status}")
    public void updateTenantStatus(@ValidTenantId @PathVariable("id") String tenantId,
                                   @NotNull @PathVariable("status") boolean status) {
        tenantService.updateTenantStatus(tenantId, status);
    }

    @PutMapping("/{id}/display-name/{display-name}")
    public void updateTenantDisplayName(@ValidTenantId @PathVariable("id") String tenantId,
                                        @ValidDisplayName @PathVariable("display-name") String displayName) {
        tenantService.updateTenantDisplayName(tenantId, displayName);
    }

    @PutMapping("/my/display-name/{display-name}")
    public void updateMyTenantDisplayName(@ValidTenantId @PathVariable("display-name") String displayName) {
        tenantService.updateMyTenantDisplayName(displayName);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant getTenant(@ValidTenantId @PathVariable("id") String tenantId) {
        Tenant tenant = tenantService.getTenant(tenantId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        return tenant;
    }

    @GetMapping("/my/profile")
    public Tenant getTenant() {
        return tenantService.getMyTenantDetails();
    }

    @DeleteMapping("/{id}")
    public void deleteTenant(@ValidTenantId @PathVariable("id") String tenantId) {
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
