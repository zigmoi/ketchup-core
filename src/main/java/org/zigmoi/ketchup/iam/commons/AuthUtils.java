package org.zigmoi.ketchup.iam.commons;


import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.exceptions.TenantIdInvalidException;

import java.util.Collection;

public class AuthUtils {

    public static Boolean matchesPolicy(String passwd) {
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,}";
        return passwd.matches(pattern);
    }

    public static String getCurrentUsername() {
        String qualifiedUserName = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            qualifiedUserName = ((UserDetails) principal).getUsername();
        } else {
            qualifiedUserName = principal.toString();
        }
        return StringUtils.substringBeforeLast(qualifiedUserName, "@");
    }

    public static String getCurrentQualifiedUsername() {
        String username = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }

    public static String getCurrentTenantId() {
        String qualifiedUserName = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            qualifiedUserName = ((UserDetails) principal).getUsername();
        } else {
            qualifiedUserName = principal.toString();
        }
        String currentTenantId = StringUtils.substringAfterLast(qualifiedUserName, "@");
        if (StringUtils.isEmpty(currentTenantId)) {
            throw new TenantIdInvalidException("Current tenant id is empty.");
        }
        return currentTenantId;
    }

    public static boolean validateTenant(String tenantId) {
        return true;
    }

    public static boolean isTenantValid(String userName) {
        String currentTenantId = AuthUtils.getCurrentTenantId();
        String tenantIdInQualifiedUserName = StringUtils.substringAfterLast(userName, "@");
        if (tenantIdInQualifiedUserName.equals(currentTenantId)) {
            return true;
        } else {
            return false;
        }
    }

}
