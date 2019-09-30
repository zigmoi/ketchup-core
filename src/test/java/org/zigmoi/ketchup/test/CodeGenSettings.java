package org.zigmoi.ketchup.test;

import org.apache.commons.lang3.StringUtils;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.TemplateUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CodeGenSettings {

    public static void main(String[] args) throws IOException {
        String[] settings = FileUtility
                .readDataFromFile(ConfigUtility.instance().getProperty("test.auto-gen.settings.list.path"))
                .split("\n");
//        String data = new CodeGenSettings().generateFromTemplate(settings, "test.auto-gen.settings.template.controller");
//        String data = new CodeGenSettings().generateFromTemplate(settings, "test.auto-gen.settings.template.service-interface");
        String data = new CodeGenSettings().generateFromTemplate(settings, "test.auto-gen.settings.template.service-impl");
        System.out.println(data);
    }

    private String generateFromTemplate(String[] settings, String templateName) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String settingName : settings) {
            Map<String, String> templateVars = getAllSettingNameVariations(settingName);
            sb.append(TemplateUtility.parse(FileUtility
                    .readDataFromFile(ConfigUtility.instance()
                            .getProperty(templateName)), templateVars))
            .append("\n");
        }
        return sb.toString();
    }

    private Map<String, String> getAllSettingNameVariations(String settingName) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put("setting-name", settingName);
        templateVars.put("setting-name-camel-case-first-upper", getSettingNameCamelCaseFirstUpper(settingName));
        templateVars.put("setting-name-lower-case", settingName.toLowerCase());
        templateVars.put("setting-name-underscore-uppercase",
                settingName.replaceAll("-", "_").toUpperCase());
        return templateVars;
    }

    private static String getSettingNameCamelCaseFirstUpper(String settingName) {
        StringBuilder sb = new StringBuilder();
        for (String part : settingName.split("-")) {
            sb.append(StringUtils.upperCase(String.valueOf(part.charAt(0)))).append(part.substring(1));
        }
        return sb.toString();
    }
}
