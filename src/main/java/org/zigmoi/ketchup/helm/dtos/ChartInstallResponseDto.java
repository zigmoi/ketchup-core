package org.zigmoi.ketchup.helm.dtos;

import lombok.Data;
import org.json.JSONObject;

@Data
public class ChartInstallResponseDto {
    String name;
    int version;
    String namespace;
    String status;
    String description;
    String firstDeployed;
    String lastDeployed;

    public ChartInstallResponseDto() {
    }

    public ChartInstallResponseDto(String commandOutput){
        JSONObject outputJson = new JSONObject(commandOutput);
        this.name= outputJson.getString("name");
        this.version= (int) outputJson.get("version");
        this.namespace = outputJson.getString("namespace");

        JSONObject info = outputJson.getJSONObject("info");
        this.status = info.getString("status");
        this.description = info.getString("description");
        this.firstDeployed = info.getString("first_deployed");
        this.lastDeployed = info.getString("last_deployed");
    }
}
