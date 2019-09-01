package org.zigmoi.ketchup.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtility {

    private final static Pattern enclosurePattern = Pattern.compile("\\$\\{(.*?)\\}");
    private final static String replaceBeginCharSeq = "\\$\\{";
    private final static String replaceEndCHarSeq = "}";

    public static String parse(String templateData, Map<String, String> templateVars) {
        if (enclosurePattern.matcher(templateData).find()) {
            List<String> valuePartKeys = getEnclosedPartsForVelocityStyleDelim(templateData);
            Map<String, String> replaceables = new HashMap<>();
            for (String partKey : valuePartKeys) {
                String partVal = templateVars.get(partKey);
                replaceables.put(partKey, partVal);
            }

            for (String key : replaceables.keySet()) {
                templateData = templateData.replaceAll(replaceBeginCharSeq + key + replaceEndCHarSeq,
                        replaceables.get(key) != null ? replaceables.get(key) : "");
            }
            return parse(templateData, templateVars);
        }
        return templateData;
    }

    private static List<String> getEnclosedPartsForVelocityStyleDelim(String str) {

        List<String> enclosedParts = new ArrayList<>();

        Matcher m = enclosurePattern.matcher(str);
        while (m.find()) {
            enclosedParts.add(m.group(1));
        }

        return enclosedParts;
    }

}
