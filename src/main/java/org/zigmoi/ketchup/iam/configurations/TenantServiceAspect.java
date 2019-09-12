package org.zigmoi.ketchup.iam.configurations;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.context.annotation.Configuration;
import org.zigmoi.ketchup.iam.services.TenantProviderService;

import static org.zigmoi.ketchup.iam.entities.TenantEntity.TENANT_FILTER_NAME;
import static org.zigmoi.ketchup.iam.entities.TenantEntity.TENANT_FILTER_ARGUMENT_NAME;

@Aspect
@Configuration
public class TenantServiceAspect {
    @Before("execution(* org.zigmoi.ketchup.iam.services.TenantProviderService+.*(..)) " +
            "&& @annotation(org.zigmoi.ketchup.iam.annotations.TenantFilter) " +
            "&& target(tenantProviderService)")
    public void before(JoinPoint joinPoint, TenantProviderService tenantProviderService) {
        tenantProviderService.entityManager
                .unwrap(Session.class)
                .enableFilter(TENANT_FILTER_NAME)
                .setParameter(TENANT_FILTER_ARGUMENT_NAME,
                        tenantProviderService.getCurrentTenantId());
    }
}
