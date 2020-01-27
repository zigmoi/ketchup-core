package org.zigmoi.ketchup.helm.dtos;

import lombok.Data;
import org.json.JSONObject;

@Data
public class ListAllChartsResponseDto {
    String name;
    String namespace;
    String revision;
    String updated;
    String status;
    String chart;
    String appVersion;

    public ListAllChartsResponseDto() {
    }

    public ListAllChartsResponseDto(String commandOutput){
        JSONObject outputJson = new JSONObject(commandOutput);
        this.name= outputJson.getString("name");
        this.namespace = outputJson.getString("namespace");
        this.revision = outputJson.getString("revision");
        this.updated = outputJson.getString("updated");
        this.status = outputJson.getString("status");
        this.chart = outputJson.getString("chart");
        this.appVersion = outputJson.getString("app_version");
    }
}
