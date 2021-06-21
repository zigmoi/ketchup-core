package org.zigmoi.ketchup.application.services;

import lombok.extern.slf4j.Slf4j;
import org.zigmoi.ketchup.application.dtos.PlatformConfigWrapperDto;
import org.zigmoi.ketchup.common.ConfigUtility;

@Slf4j
public class PlatformConfigReader {

    private PlatformConfigWrapperDto configs;
    private static PlatformConfigReader reader;
    public static PlatformConfigReader instance(){
        if (reader == null){
            reader = new PlatformConfigReader();
        }
        return reader;
    }

    private PlatformConfigReader(){
        configs = new PlatformConfigWrapperDto();
        String configFile = ConfigUtility.instance().getProperty("app.platform-config.path");
    }
}
