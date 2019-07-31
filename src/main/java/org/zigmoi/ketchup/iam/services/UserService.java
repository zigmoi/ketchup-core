package org.zigmoi.ketchup.iam.services;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.zigmoi.ketchup.iam.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    // @PreAuthorize("T(org.zigmoi.ketchup.iam.commons.AuthUtils).isTenantValid(authentication.name) and hasRole('ROLE_TENANT_ADMIN')")
    void createUser(User user);

    // @PreAuthorize("T(org.zigmoi.ketchup.iam.commons.AuthUtils).isTenantValid(authentication.name) and hasRole('ROLE_TENANT_ADMIN')")
    // @PostAuthorize("hasRole('ROLE_TENANT_ADMIN') or returnObject.isPresent()?returnObject.get().getUsername()==authentication.name:false")
    Optional<User> getUser(String userName);

    // @PreAuthorize("T(org.zigmoi.ketchup.iam.commons.AuthUtils).isTenantValid(authentication.name) and hasRole('ROLE_TENANT_ADMIN')")
    void updateUserStatus(String userName, boolean status);

    // @PreAuthorize("T(org.zigmoi.ketchup.iam.commons.AuthUtils).isTenantValid(authentication.name) and hasRole('ROLE_TENANT_ADMIN')")
    void updateUserDisplayName(String userName, String displayName);

    // @PreAuthorize("T(org.zigmoi.ketchup.iam.commons.AuthUtils).isTenantValid(authentication.name) and hasRole('ROLE_TENANT_ADMIN')")
    void deleteUser(String userName);

    // @PreAuthorize("T(org.zigmoi.ketchup.iam.commons.AuthUtils).isTenantValid(authentication.name) and hasRole('ROLE_TENANT_ADMIN')")
    List<User> listAllUsers();
}