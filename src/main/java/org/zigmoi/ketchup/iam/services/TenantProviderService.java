package org.zigmoi.ketchup.iam.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zigmoi.ketchup.iam.commons.AuthUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
@Slf4j
public abstract class TenantProviderService {

    @PersistenceContext
    public EntityManager entityManager;

    public String getCurrentTenantId() {
        String tenantId = AuthUtils.getCurrentTenantId();
        if (log.isDebugEnabled()) {
            log.debug("Tenant Provider fetching current tenant: " + tenantId);
        }
        return tenantId;
    }
}
