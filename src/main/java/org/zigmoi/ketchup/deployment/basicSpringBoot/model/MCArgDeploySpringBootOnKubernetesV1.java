package org.zigmoi.ketchup.deployment.basicSpringBoot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MCArgDeploySpringBootOnKubernetesV1 {

    private String kubeconfigFilePath, namespace, appId;
    private String vmVendor, dockerBuildImageName, dockerBuildImageTag;
    private Map<String, List<String>> ipHostnameMap = new HashMap<>();
    private String dockerRegistryVendor;
    private DockerVendorArg dockerVendorArg;
    private int port;

    public MCArgDeploySpringBootOnKubernetesV1 kubeconfigFilePath(String basePath) {
        this.kubeconfigFilePath = basePath;
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 ipHostnameMap(String ip, String hostname) {
        this.ipHostnameMap.computeIfAbsent(ip, k -> new ArrayList<>()).add(hostname);
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 dockerVendorArg(DockerVendorArg dockerVendorArg) {
        this.dockerVendorArg = dockerVendorArg;
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 dockerBuildImageTag(String dockerBuildImageTag) {
        this.dockerBuildImageTag = dockerBuildImageTag;
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 dockerBuildImageName(String dockerBuildImageName) {
        this.dockerBuildImageName = dockerBuildImageName;
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 appId(String appId) {
        this.appId = appId;
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 vmVendor(String vmVendor) {
        this.vmVendor = vmVendor;
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 dockerRegistryVendor(String dockerRegistryVendor) {
        this.dockerRegistryVendor = dockerRegistryVendor;
        return this;
    }

    public MCArgDeploySpringBootOnKubernetesV1 port(int port) {
        this.port = port;
        return this;
    }

    @Data
    public static class DockerVendorArg {

        private String registryId, repo, registryBaseUrl, awsAccessKeyId, awsSecretKey;

        public DockerVendorArg registryId(String registryId) {
            this.registryId = registryId;
            return this;
        }

        public DockerVendorArg registryBaseUrl(String registryBaseUrl) {
            this.registryBaseUrl = registryBaseUrl;
            return this;
        }

        public DockerVendorArg awsAccessKeyId(String awsAccessKeyId) {
            this.awsAccessKeyId = awsAccessKeyId;
            return this;
        }

        public DockerVendorArg awsSecretKey(String awsSecretKey) {
            this.awsSecretKey = awsSecretKey;
            return this;
        }

        public DockerVendorArg repo(String repo) {
            this.repo = repo;
            return this;
        }
    }
}
