package org.zigmoi.ketchup.common;

import java.util.Base64;

public class StringUtility {

    public static String decodeBase64(String base64Str) {
        return new String(Base64.getDecoder().decode(base64Str));
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
