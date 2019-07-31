package org.zigmoi.ketchup.iam.common;


import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    public static Boolean matchesPolicy(String passwd) {
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,}";
        return passwd.matches(pattern);
    }

    public static String getCurrentUsername() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return StringUtils.substringBeforeLast(currentUser, "@");
    }

    public static String getCurrentTenantId() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return StringUtils.substringAfterLast(currentUser, "@");
    }

    public static boolean validateTenant(String tenantId) {
        return true;
    }

    public static boolean isTenantValid(String userName) {
        return userName.endsWith("@zigmoi.com");
    }

}
