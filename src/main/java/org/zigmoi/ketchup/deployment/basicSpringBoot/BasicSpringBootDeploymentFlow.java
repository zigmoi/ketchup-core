package org.zigmoi.ketchup.deployment.basicSpringBoot;

import io.kubernetes.client.ApiException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.deployment.basicSpringBoot.model.*;
import org.zigmoi.ketchup.exception.ConfigurationException;
import org.zigmoi.ketchup.exception.UnexpectedException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BasicSpringBootDeploymentFlow {
    private JSONObject config;

    private String id;

    public BasicSpringBootDeploymentFlow(JSONObject config) {
        this.config = config;
        this.id = UUID.randomUUID().toString();
    }

    public void execute() {
        JSONArray stages = config.getJSONArray("stages");
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
            case BasicSpringBootDeploymentFlowConstants.C_PULL_FROM_REMOTE: {
                IBasicSpringBootDeploymentCommands commands = new BasicSpringBootDeploymentCommandsFlow();
                return commands.pullFromRemote(getArgPullFromRemote(args));
//                return new MCommandStatus();
            }
            case BasicSpringBootDeploymentFlowConstants.C_MAVEN_CLEAN_INSTALL: {
                IBasicSpringBootDeploymentCommands commands = new BasicSpringBootDeploymentCommandsFlow();
                return commands.mvnInstall(getArgMvnInstallV1(args));
//                return new MCommandStatus();
            }
            case BasicSpringBootDeploymentFlowConstants.C_BUILD_SPRING_BOOT_DOCKER_IMAGE: {
                IBasicSpringBootDeploymentCommands commands = new BasicSpringBootDeploymentCommandsFlow();
//                return commands.buildSprintBootDockerImage(getArgBuildSpringBootDockerImageV1(args));
                return new MCommandStatus();
            }
            case BasicSpringBootDeploymentFlowConstants.C_DEPLOY_IN_KUBERNETES: {
                IBasicSpringBootDeploymentCommands commands = new BasicSpringBootDeploymentCommandsFlow();
                return commands.deployInKubernetes(getArgBuildSpringBootKubernetesDeployV1(args));
//                return new MCommandStatus();
            }
            default:
                throw new ConfigurationException("Unknown command : " + command);
        }
    }

    private Object getArgBuildSpringBootKubernetesDeployV1(JSONArray args) {

        JSONObject argJ = args.getJSONObject(0);
        JSONObject vendorArg = argJ.getJSONObject("docker-registry-vendor-args");
        JSONObject ipHostnameMap = (argJ.has("ip-hostname-map") ? argJ.getJSONObject("ip-hostname-map") : null);

        MCArgDeploySpringBootOnKubernetesV1 mcArgDeploySpringBootOnKubernetesV1 = new MCArgDeploySpringBootOnKubernetesV1()
                .kubeconfigFilePath(argJ.getString("kubeconfig-file-path"))
                .namespace(argJ.getString("namespace"))
                .appId(argJ.getString("app-id"))
                .deploymentName(getDeploymentName(argJ.getString("app-id")))
                .patchDeploymentIfAlreadyExists(Boolean.parseBoolean(argJ.getString("patch-deployment-if-exists")))
                .vmVendor(argJ.getString("vm-vendor"))
                .dockerRegistryVendor(argJ.getString("docker-registry-vendor"))
                .dockerVendorArg(new MCArgDeploySpringBootOnKubernetesV1.DockerVendorArg()
                    .repo(vendorArg.getString("repo"))
                    .registryId(vendorArg.getString("registry-id"))
                    .registryBaseUrl(vendorArg.getString("registry-base-url"))
                    .awsAccessKeyId(vendorArg.getString("aws-access-key-id"))
                    .awsSecretKey(vendorArg.getString("aws-secret-key"))
                )
                .dockerBuildImageName(argJ.getString("docker-build-image-name"))
                .dockerBuildImageTag(argJ.getString("docker-build-image-tag"))
                .port(Integer.parseInt(argJ.getString("port")));

        if (ipHostnameMap != null) {
            for (String ip : ipHostnameMap.keySet()) {
                JSONArray hostnameListArray = ipHostnameMap.getJSONArray(ip);
                for (Object hostnameO : hostnameListArray) {
                    mcArgDeploySpringBootOnKubernetesV1.ipHostnameMap(ip, (String) hostnameO);
                }
            }
        }

        return mcArgDeploySpringBootOnKubernetesV1;
    }

    private Object getArgBuildSpringBootDockerImageV1(JSONArray args) {
        JSONObject argJ = args.getJSONObject(0);
        JSONObject vendorArg = argJ.getJSONObject("docker-registry-vendor-args");
        JSONObject dockerFileTemplateArgs = argJ.getJSONObject("docker-file-template-args");
        MCArgBuildSpringBootDockerImageV1 mcArgBuildSpringBootDockerImageV1 = new MCArgBuildSpringBootDockerImageV1()
                .basePath(argJ.getString("base-path"))
                .dockerFilePath(argJ.getString("docker-file-path"))
                .dockerFileTemplatePath(argJ.getString("docker-file-template-path"))
                .dockerRegistryVendor(argJ.getString("docker-registry-vendor"))
                .dockerVendorArg(new MCArgBuildSpringBootDockerImageV1.DockerVendorArg()
                    .repo(vendorArg.getString("repo"))
                    .registryId(vendorArg.getString("registry-id"))
                    .registryBaseUrl(vendorArg.getString("registry-base-url"))
                    .awsAccessKeyId(vendorArg.getString("aws-access-key-id"))
                    .awsSecretKey(vendorArg.getString("aws-secret-key"))
                )
                .dockerBuildImageName(argJ.getString("docker-build-image-name"))
                .dockerBuildImageTag(argJ.getString("docker-build-image-tag"))
        ;
        for (String k : dockerFileTemplateArgs.keySet()) {
            mcArgBuildSpringBootDockerImageV1.putDockerFileTemplateArg(k, dockerFileTemplateArgs.getString(k));
        }
        return mcArgBuildSpringBootDockerImageV1;
    }

    private Object getArgMvnInstallV1(JSONArray args) {
        JSONObject argJ = args.getJSONObject(0);
        MCArgMvnInstallV1 argMvnInstallV1 = new MCArgMvnInstallV1();
        argMvnInstallV1.setBasePath(argJ.getString("base-path"));
        argMvnInstallV1.setRepoName(argJ.getString("repo-name"));
        argMvnInstallV1.setBuildPath(argJ.getString("build-path"));
        argMvnInstallV1.setMvnCommandPath(argJ.getString("maven-command-path"));
        argMvnInstallV1.setPrivateRepoSettingsPath(argJ.getString("maven-private-repo-settings-path"));
        if (argJ.has("branch-name")) {
            argMvnInstallV1.setBranchName(argJ.getString("branch-name"));
        }
        if (argJ.has("commit-id")) {
            argMvnInstallV1.setCommitId(argJ.getString("commit-id"));
        }
        return argMvnInstallV1;
    }

    private Object getArgPullFromRemote(JSONArray args) {
        validateArgPullFromRemote(args);
        return getArgPullFromRemoteV1(args);
    }

    private Object getArgPullFromRemoteV1(JSONArray args) {
        JSONObject argJ = args.getJSONObject(0);
        JSONObject vendorArg = argJ.getJSONObject("git-vendor-arg");
        return new MCArgPullFromRemoteV1()
                .gitVendor(argJ.getString("git-vendor"))
                .basePath(argJ.getString("base-path"))
                .repoName(argJ.getString("repo-name"))
                .gitVendorArg(new MCArgPullFromRemoteV1.GitVendorArg()
                        .url(vendorArg.getString("url"))
                        .username(vendorArg.getString("username"))
                        .password(vendorArg.getString("password"))
                );
    }

    private void validateArgPullFromRemote(JSONArray args) { // todo
        if (args == null || args.length() != 1) {
            throw new ConfigurationException("Invalid args");
        }
    }

    public String getId() {
        return this.id;
    }

    public boolean isDeploymentRunning() throws IOException, ApiException {
        File kubeConfig = new File(getDeploymentStageArg().getKubeconfigFilePath());
        String appId = getDeploymentStageArg().getAppId();
        String namespace = getDeploymentStageArg().getNamespace();
        return KubernetesUtility.deploymentAlreadyExists(kubeConfig, appId, namespace, getDeploymentName(appId));
    }

    private MCArgDeploySpringBootOnKubernetesV1 getDeploymentStageArg() {
        JSONArray stages = config.getJSONArray("stages");
        for (int i = 0; i < stages.length(); i++) {
            JSONObject stageO = stages.getJSONObject(i);
            String command = stageO.getString("command");
            JSONArray args = stageO.getJSONArray("args");
            if (BasicSpringBootDeploymentFlowConstants.C_DEPLOY_IN_KUBERNETES.equals(command)) {
                return (MCArgDeploySpringBootOnKubernetesV1) getArgBuildSpringBootKubernetesDeployV1(args);
            }
        }
        throw new UnexpectedException("Deployment stage info not found");
    }

    public static String getDeploymentName(String appId) {
        return appId+"-deployment";
    }

    public void validate() {
        try {
            if (isDeploymentRunning() && !getDeploymentStageArg().isPatchDeploymentIfAlreadyExists()) {
                throw new UnexpectedException("Deployment already exists and patchDeploymentIfAlreadyExists is false");
            }
        } catch (IOException | ApiException e) {
            throw new UnexpectedException("Failed checking if deployment already exists", e);
        }
    }

    public static void main(String[] args) throws IOException {
        String flowConfigFile = "/home/tapo/IdeaProjects/zigmoi/ketchup/ketchup-core/conf/private/deployment_flow_config_sample.json";
        String deploymentFlowConfigJSON = FileUtility.readDataFromFile(new File(flowConfigFile));
        new BasicSpringBootDeploymentFlow(new JSONObject(deploymentFlowConfigJSON)).execute();
    }
}
