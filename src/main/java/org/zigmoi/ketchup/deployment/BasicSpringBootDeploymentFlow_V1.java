package org.zigmoi.ketchup.deployment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zigmoi.ketchup.common.FileUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BasicSpringBootDeploymentFlow_V1 {

    public void execute(String config) {
        JSONObject jo = new JSONObject(config);
        JSONArray stages = new JSONArray(jo.getJSONArray("stages"));
        for (int i = 0; i < stages.length(); i++) {
            JSONObject stageO = new JSONObject(stages.getJSONObject(i));
            String command = stageO.getString("command");
            JSONArray args = new JSONArray(stageO.getJSONArray("args"));
            List<String> commandLogs = execCommand(command, args);
        }
    }

    private List<String> execCommand(String command, JSONArray args) {
        switch (command) {
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String flowConfigFile = "/home/tapo/IdeaProjects/zigmoi/ketchup/ketchup-core/docs/deployment_flow_config_sample.json";
        String deploymentFlowConfigJSON = FileUtility.readDataFromFile(new File(flowConfigFile));
        new BasicSpringBootDeploymentFlow_V1().execute(deploymentFlowConfigJSON);
    }
}
