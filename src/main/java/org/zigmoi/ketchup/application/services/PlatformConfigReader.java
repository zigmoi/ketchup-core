package org.zigmoi.ketchup.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.zigmoi.ketchup.application.dtos.PlatformConfigWrapperDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class PlatformConfigReader {

    private PlatformConfigWrapperDto configs;
    private static PlatformConfigReader reader;
    private String fileName = "platform-conf.json";
    private PlatformConfigWrapperDto platformConfigWrapper;

    public static PlatformConfigReader instance(){
        if (reader == null){
            reader = new PlatformConfigReader();
        }
        return reader;
    }

    private PlatformConfigReader(){
        configs = new PlatformConfigWrapperDto();
        load();
    }

    private void load(){
        InputStream in = PlatformConfigReader.class.getClassLoader().getResourceAsStream(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        JSONTokener tokener = new JSONTokener(bufferedReader);
        JSONArray jsonArray = new JSONArray(tokener);
        ObjectMapper objectMapper = new ObjectMapper();
        PlatformConfigWrapperDto dto = new PlatformConfigWrapperDto();
        for (Object o: jsonArray){
            JSONObject item = (JSONObject) o;
            try {
                PlatformConfigWrapperDto.PlatformConfigDto configDto = objectMapper.readValue(String.valueOf(item), PlatformConfigWrapperDto.PlatformConfigDto.class);
                dto.add(configDto);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        this.platformConfigWrapper = dto;
    }

    public PlatformConfigWrapperDto getPlatformConfig(){
        return platformConfigWrapper;
    }

    public void reload(boolean force){
        if (force){
            load();
        }
    }
}
