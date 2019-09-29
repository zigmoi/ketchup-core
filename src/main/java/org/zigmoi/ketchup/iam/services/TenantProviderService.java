package org.zigmoi.ketchup.iam.services;

import org.springframework.stereotype.Service;
import org.zigmoi.ketchup.iam.commons.AuthUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public abstract class TenantProviderService {

    @PersistenceContext
    public EntityManager entityManager;

    public String getCurrentTenantId() {
        String tenantId = AuthUtils.getCurrentTenantId();
        System.out.println("Tenant Provider fetching current tenant: " + tenantId);
        return tenantId;
    }
}
