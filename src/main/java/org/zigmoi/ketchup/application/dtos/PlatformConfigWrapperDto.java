package org.zigmoi.ketchup.application.dtos;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PlatformConfigWrapperDto {

    private List<PlatformConfigDto> dtos;

    public PlatformConfigDto getPlatformConfig(String platformName){
        for (PlatformConfigDto node: dtos){
            if (node.getName().equalsIgnoreCase(platformName)){
                return node;
            }
        }
        return null;
    }

    public boolean isValid(String platformName, String buildTool){
        PlatformConfigDto dto = getPlatformConfig(platformName);
        return dto != null && dto.isValidBuildTool(buildTool);
    }

    @Data
    public static class PlatformConfigDto {
        private String name;
        private List<Map<String, Object>> config;

        public boolean isValidBuildTool(String buildTool){
            for (Map<String, Object> node: config){
                if (String.valueOf(node.get("buildTool")).equalsIgnoreCase(buildTool)){
                    return true;
                }
            }
            return false;
        }
    }
}
