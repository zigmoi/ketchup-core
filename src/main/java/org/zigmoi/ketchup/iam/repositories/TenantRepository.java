package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.iam.entities.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, String> {
}
