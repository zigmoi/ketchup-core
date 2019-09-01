package org.zigmoi.ketchup.deployment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.deployment.model.MCArgBuildSpringBootDockerImageV1;
import org.zigmoi.ketchup.deployment.model.MCArgMvnInstallV1;
import org.zigmoi.ketchup.deployment.model.MCArgPullFromRemoteV1;
import org.zigmoi.ketchup.deployment.model.MCommandStatus;
import org.zigmoi.ketchup.exception.KConfigurationException;

import java.io.File;
import java.io.IOException;

public class BasicSpringBootDeploymentFlow {

    public void execute(String config) {
        JSONObject jo = new JSONObject(config);
        JSONArray stages = jo.getJSONArray("stages");
        for (int i = 0; i < stages.length(); i++) {
            JSONObject stageO = stages.getJSONObject(i);
            String command = stageO.getString("command");
            JSONArray args = stageO.getJSONArray("args");
            MCommandStatus commandLogs = execCommand(command, args);
            System.out.println(new JSONObject(commandLogs));
        }
    }

    private MCommandStatus execCommand(String command, JSONArray args) {
        switch (command) {
            case DeploymentFlowConstants.C_PULL_FROM_REMOTE: {
                MCArgPullFromRemoteV1 arg = (MCArgPullFromRemoteV1) getArgPullFromRemote(args);
                IBasicSpringBootDeploymentCommands commands = new BasicSpringBootDeploymentCommandsFlow();
                return commands.pullFromRemote(arg);
            }
            case DeploymentFlowConstants.C_MAVEN_CLEAN_INSTALL: {
                MCArgMvnInstallV1 arg = (MCArgMvnInstallV1) getArgMvnInstallV1(args);
                IBasicSpringBootDeploymentCommands commands = new BasicSpringBootDeploymentCommandsFlow();
                return commands.mvnInstall(arg);
            }
            case DeploymentFlowConstants.C_BUILD_SPRING_BOOT_DOCKER_IMAGE: {
                MCArgBuildSpringBootDockerImageV1 arg = (MCArgBuildSpringBootDockerImageV1) getArgBuildSpringBootDockerImageV1(args);
                IBasicSpringBootDeploymentCommands commands = new BasicSpringBootDeploymentCommandsFlow();
                return commands.buildSprintBootDockerImage(arg);
            }
            default:
                throw new KConfigurationException("Unknown command : " + command);
        }
    }

    private Object getArgBuildSpringBootDockerImageV1(JSONArray args) {
        return null;
    }

    private Object getArgMvnInstallV1(JSONArray args) {
        JSONObject argJ = args.getJSONObject(0);
        MCArgMvnInstallV1 argMvnInstallV1 = new MCArgMvnInstallV1();
        argMvnInstallV1.setBasePath(argJ.getString("base-path"));
        argMvnInstallV1.setRepoName(argJ.getString("repo-name"));
        argMvnInstallV1.setBuildPath(argJ.getString("build-path"));
        argMvnInstallV1.setMvnCommandPath(argJ.getString("maven-command-path"));
        argMvnInstallV1.setPrivateRepoSettingsPath(argJ.getString("maven-private-repo-settings-path"));
        argMvnInstallV1.setBranchName(argJ.getString("branch-name"));
        argMvnInstallV1.setCommitId(argJ.getString("commit-id"));
        return argMvnInstallV1;
    }

    private Object getArgPullFromRemote(JSONArray args) {
        validateArgPullFromRemote(args);
        return getArgPullFromRemoteV1(args);
    }

    private MCArgPullFromRemoteV1 getArgPullFromRemoteV1(JSONArray args) {
        JSONObject argJ = args.getJSONObject(0);
        MCArgPullFromRemoteV1 argPullFromRemoteV1 = new MCArgPullFromRemoteV1();
        argPullFromRemoteV1.setVendor(argJ.getString("vendor"));
        argPullFromRemoteV1.setUrl(argJ.getString("url"));
        argPullFromRemoteV1.setBasePath(argJ.getString("base-path"));
        argPullFromRemoteV1.setRepoName(argJ.getString("repo-name"));
        argPullFromRemoteV1.setUsername(argJ.getString("username"));
        argPullFromRemoteV1.setPassword(argJ.getString("password"));
        return argPullFromRemoteV1;
    }

    private void validateArgPullFromRemote(JSONArray args) { // todo
        if (args == null || args.length() != 1) {
            throw new KConfigurationException("Invalid args");
        }
    }

    public static void main(String[] args) throws IOException {
        String flowConfigFile = "/home/tapo/IdeaProjects/zigmoi/ketchup/ketchup-core/conf/private/deployment_flow_config_sample.json";
        String deploymentFlowConfigJSON = FileUtility.readDataFromFile(new File(flowConfigFile));
        new BasicSpringBootDeploymentFlow().execute(deploymentFlowConfigJSON);
    }
}
