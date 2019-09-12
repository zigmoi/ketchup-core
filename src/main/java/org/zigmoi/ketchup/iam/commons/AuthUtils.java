package org.zigmoi.ketchup.iam.commons;


import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
        return StringUtils.substringAfterLast(qualifiedUserName, "@");
    }

    public static boolean validateTenant(String tenantId) {
        return true;
    }

    public static boolean isTenantValid(String userName) {
        return userName.endsWith("@zigmoi.com");
    }

}
