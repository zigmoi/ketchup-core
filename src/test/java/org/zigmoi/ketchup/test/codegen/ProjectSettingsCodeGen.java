package org.zigmoi.ketchup.test.codegen;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.json.JSONObject;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.TemplateUtility;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProjectSettingsCodeGen {

    private JSONObject schema;
    private String dtoTemplate;
    private String controllerTemplate;
    private String serviceInterfaceTemplate;
    private String serviceImplTemplate;
    private String basePackage;
    private String basePath;

    public static void main(String[] args) throws IOException, ParseException {
        ProjectSettingsCodeGen codeGen = new ProjectSettingsCodeGen();
        codeGen.setBasePath(ConfigUtility.instance().getProperty("ketchup.dir.base"));
        codeGen.setBasePackage("org.zigmoi.ketchup.project");
        codeGen.setSchema(new JSONObject(
                FileUtility
                        .readDataFromFile(ConfigUtility.instance().getProperty("code-gen.project-settings.schema"))
        ));
        codeGen.setDtoTemplate(
                FileUtility
                        .readDataFromFile(ConfigUtility.instance()
                                .getProperty("code-gen.project-settings.template.dto"))
        );
        codeGen.setControllerTemplate(
                FileUtility
                        .readDataFromFile(ConfigUtility.instance()
                                .getProperty("code-gen.project-settings.template.controller"))
        );
        codeGen.setServiceInterfaceTemplate(
                FileUtility
                        .readDataFromFile(ConfigUtility.instance()
                                .getProperty("code-gen.project-settings.template.service-interface"))
        );
        codeGen.setServiceImplTemplate(
                FileUtility
                        .readDataFromFile(ConfigUtility.instance()
                                .getProperty("code-gen.project-settings.template.service-impl"))
        );
        String data = null;
        for (MSettingDef def : codeGen.getSettingsDef()) {
            data = codeGen.renderDto(def);
        }
        System.out.println(data);
//        System.out.println(codeGen.renderController());
//        System.out.println(codeGen.renderServiceInterface());
//        System.out.println(codeGen.renderServiceImpl());
    }

    private String renderController() throws ParseException {
        return renderTemplate(getSettingsDef(), "controller", getControllerTemplate());
    }

    private String renderServiceInterface() throws ParseException {
        return renderTemplate(getSettingsDef(), "service-interface", getServiceInterfaceTemplate());
    }

    private String renderServiceImpl() throws ParseException {
        return renderTemplate(getSettingsDef(), "service-impl", getServiceImplTemplate());
    }

    private String renderDto(MSettingDef def) throws ParseException {
        return renderTemplate(def, "dto", getDtoTemplate());
    }

    private String renderTemplate(List<MSettingDef> def, String name, String templateData) throws ParseException {
        VelocityContext context = new VelocityContext();
        context.put("base_package", basePackage);
        context.put("setting_defs", def);
        RuntimeServices rs = RuntimeSingleton.getRuntimeServices();
        StringReader sr = new StringReader(templateData);
        SimpleNode sn = rs.parse(sr, name);
        Template t = new Template();
        t.setRuntimeServices(rs);
        t.setData(sn);
        t.initDocument();
        StringWriter sw = new StringWriter();
        t.merge(context, sw);
        return sw.toString();
    }

    private String renderTemplate(MSettingDef def, String name, String templateData) throws ParseException {
        VelocityContext context = new VelocityContext();
        context.put("base_package", basePackage);
        context.put("setting_name_camel_case_first_upper", def.getSetting_name_camel_case_first_upper());
        context.put("setting_field_defs", def.getFieldDefs());
        RuntimeServices rs = RuntimeSingleton.getRuntimeServices();
        StringReader sr = new StringReader(templateData);
        SimpleNode sn = rs.parse(sr, name);
        Template t = new Template();
        t.setRuntimeServices(rs);
        t.setData(sn);
        t.initDocument();
        StringWriter sw = new StringWriter();
        t.merge(context, sw);
        return sw.toString();
    }

    private List<MSettingDef> getSettingsDef() {
        List<MSettingDef> defs = new ArrayList<>();
        for (Map.Entry<String, Object> schemaEntry : schema.toMap().entrySet()) {
            String settingName = schemaEntry.getKey();
            ArrayList settingSchema = (ArrayList) schemaEntry.getValue();
            MSettingDef settingDef = new MSettingDef();
            settingDef.setSetting_name_underscore_uppercase(settingName.replaceAll("-", "_").toUpperCase());
            settingDef.setSetting_name_lower_case(settingName.toLowerCase());
            settingDef.setSetting_name_camel_case_first_upper(getCamelCaseFirstUpper(settingName));
            for (Object o : settingSchema) {
                Map settingSchemaField = (Map) o;
                String fieldName = (String) settingSchemaField.keySet().iterator().next();
                String dataType = (String) settingSchemaField.get(fieldName);
                MSettingDef.MSettingFieldDef settingFieldDef = new MSettingDef.MSettingFieldDef();
                settingFieldDef.setName(fieldName);
                settingFieldDef.setData_type(dataType);
                settingFieldDef.setField_name_camel_case_first_upper(getCamelCaseFirstUpper(fieldName));
                settingFieldDef.setField_name_camel_case_first_lower(getCamelCaseFirstLower(fieldName));
                settingDef.add(settingFieldDef);
            }
            defs.add(settingDef);
        }
        return defs;
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
        templateVars.put("setting-name-camel-case-first-upper", getCamelCaseFirstUpper(settingName));
        templateVars.put("setting-name-lower-case", settingName.toLowerCase());
        templateVars.put("setting-name-underscore-uppercase",
                settingName.replaceAll("-", "_").toUpperCase());
        return templateVars;
    }

    private static String getCamelCaseFirstUpper(String settingName) {
        StringBuilder sb = new StringBuilder();
        for (String part : settingName.split("-")) {
            sb.append(StringUtils.upperCase(String.valueOf(part.charAt(0)))).append(part.substring(1));
        }
        return sb.toString();
    }

    private static String getCamelCaseFirstLower(String settingName) {
        StringBuilder sb = new StringBuilder();
        for (String part : settingName.split("-")) {
            sb.append(StringUtils.lowerCase(String.valueOf(part.charAt(0)))).append(part.substring(1));
        }
        return sb.toString();
    }
}
