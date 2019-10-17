package org.zigmoi.ketchup.test.codegen;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MSettingDef {

    private String setting_name_underscore_uppercase;
    private String setting_name_camel_case_first_upper;
    private String setting_name_lower_case;
    private List<MSettingFieldDef> fieldDefs = new ArrayList<>();

    public void add(MSettingFieldDef fieldDef) {
        fieldDefs.add(fieldDef);
    }

    @Data
    public static class MSettingFieldDef {
        private String data_type;
        private String name;
        private String field_name_camel_case_first_upper;
        private String field_name_camel_case_first_lower;
        private String data_type_java;
    }
}
