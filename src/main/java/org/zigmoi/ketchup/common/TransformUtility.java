package org.zigmoi.ketchup.common;

import java.util.LinkedHashMap;
import java.util.Map;

public class TransformUtility {
    public static Map<String, String> convertToMapStringString(Map<String, Object> data) {
        Map<String, String> res = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            res.put(entry.getKey(), entry.getValue().toString());
        }
        return res;
    }
}
